package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.dto.GetTrainRecordDTO;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.im.bean.SocketFrameTextMessage;
import cn.cuptec.faros.im.core.UserChannelManager;
import cn.cuptec.faros.im.proto.ChatProto;
import cn.cuptec.faros.service.*;
import cn.cuptec.faros.util.ThreadPoolExecutorFactory;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 训练记录
 */
@RestController
@RequestMapping("/planUserTrainRecord")
public class PlanUserTrainReordController extends AbstractBaseController<PlanUserTrainRecordService, TbUserTrainRecord> {
    @Resource
    private TrainDataService trainDataService;
    @Resource
    private SubPlanService subPlanService;
    @Resource
    private PlanUserService planUserService;
    @Resource
    private PatientUserService patientUserService;
    @Resource
    private UserService userService;
    @Resource
    private UniAppPushService uniAppPushService;
    @Resource
    private ChatUserService chatUserService;
    @Resource
    private ChatMsgService chatMsgService;

    @GetMapping("/pageByUid/{uid}")
    public RestResponse pageByUid(@PathVariable String uid) {

        Page<TbUserTrainRecord> page = getPage();

        return RestResponse.ok(service.pageByUid(page, uid));
    }

    @PostMapping("/save")
    public RestResponse<TbUserTrainRecord> saveAndData(@RequestBody List<TbUserTrainRecord> userTrainRecordList) {
        //判断异常 推送给医生
        String userId = userTrainRecordList.get(0).getUserId();
        List<TbTrainUser> list = planUserService.list(new QueryWrapper<TbTrainUser>().lambda().eq(TbTrainUser::getUserId, userId));
        pushData(list, userTrainRecordList);


        service.saveAndData(userTrainRecordList);
        return RestResponse.ok();
    }

    private void pushData(List<TbTrainUser> list, List<TbUserTrainRecord> userTrainRecordList) {
        ThreadPoolExecutorFactory.getThreadPoolExecutor().execute(new Runnable() {
            @Override
            public void run() {
                if (!CollectionUtils.isEmpty(list)) {
                    TbTrainUser tbTrainUser = list.get(0);
                    Integer xtUserId = tbTrainUser.getXtUserId();
                    if (xtUserId != null) {
                        User user = userService.getById(xtUserId);
                        if (user != null) {
                            List<ChatUser> chatUsers = chatUserService.list(new QueryWrapper<ChatUser>().lambda().eq(ChatUser::getTargetUid, xtUserId));
                            if (!CollectionUtils.isEmpty(chatUsers)) {
                                for (TbUserTrainRecord tbUserTrainRecord : userTrainRecordList) {
                                    Long keyId = tbUserTrainRecord.getKeyId();
                                    Integer painLevel = tbUserTrainRecord.getPainLevel();//vas值
                                    if (painLevel != null) {
                                        if (painLevel > 2) {
                                            //发送信息
                                            push(chatUsers, "VAS异常", tbUserTrainRecord.getKeyId(), user.getId());
                                        }
                                    }
                                    String adverseReactions = tbUserTrainRecord.getAdverseReactions();//异常反馈
                                    if (adverseReactions != null) {
                                        //发送信息
                                        push(chatUsers, "异常反馈", tbUserTrainRecord.getKeyId(), user.getId());
                                    }
                                    Integer successTime = tbUserTrainRecord.getSuccessTime();
                                    Integer warningTime = tbUserTrainRecord.getWarningTime();
                                    Integer time = 0;
                                    if (warningTime != null) {
                                        time = time + warningTime;
                                    }
                                    if (successTime != null) {
                                        time = time + warningTime;
                                    }
                                    if (time != 0 && tbUserTrainRecord.getTotalTrainStep() != null) {
                                        //发送信息
                                        push(chatUsers, "踩踏次数异常", tbUserTrainRecord.getKeyId(), user.getId());
                                    }
                                }


                            }

                        }

                    }
                }
            }
        });
    }

    private void push(List<ChatUser> chatUsers, String msg, Long keyId, Integer fromUserId) {
        List<ChatMsg> list = chatMsgService.list(new QueryWrapper<ChatMsg>().lambda().eq(ChatMsg::getStr1, keyId));
        if (!CollectionUtils.isEmpty(list)) {
            return;
        }
        for (ChatUser chatUser : chatUsers) {
            //添加一条聊天记录Integer targetUid, Long keyId, String msg, String
            // msgType, Integer fromUserId, Integer patientId, Date date, Integer chatUserId
            ChatMsg chatMsg = saveChatMsg(chatUser.getUid(), keyId, msg, ChatProto.CATH, fromUserId, chatUser.getPatientId()
                    , new Date(), chatUser.getId());
            if (chatUser.getGroupType().equals(1)) {
                //群聊
                String data = chatUser.getUserIds();
                List<String> allUserIds = Arrays.asList(data.split(","));
                sendNotic(chatMsg, fromUserId, chatUser.getPatientId(), allUserIds, chatUser.getId());
            } else {
                //单聊
                Channel targetUserChannel = UserChannelManager.getUserChannel(chatUser.getUid());
                //向目标用户发送新消息提醒
                SocketFrameTextMessage targetUserMessage
                        = SocketFrameTextMessage.newMessageTip(fromUserId, "", "", new Date(), chatMsg.getMsgType(), JSON.toJSONString(chatMsg));

                if (targetUserChannel != null) {
                    targetUserChannel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(targetUserMessage)));
                } else {
                    String patientId = chatUser.getPatientId();
                    User user = userService.getById(fromUserId);

                    String name = "";
                    if (StringUtils.isEmpty(user.getPatientName())) {
                        name = user.getNickname();
                    } else {
                        name = user.getPatientName();
                    }
                    if (StringUtils.isEmpty(patientId)) {
                        PatientUser patientUser = patientUserService.getById(patientId);
                        name = patientUser.getName();
                    }
                    uniAppPushService.send("法罗适", name + ": " + chatMsg.getMsg(), chatUser.getUid() + "", "");

                }
            }

        }

    }

    public ChatMsg saveChatMsg(Integer targetUid, Long keyId, String msg, String msgType, Integer fromUserId, String patientId, Date date, Integer chatUserId) {
        ChatMsg chatMsg = new ChatMsg();
        chatMsg.setMsgType(msgType);
        chatMsg.setFromUid(fromUserId);
        if (!StringUtils.isEmpty(patientId)) {
            chatMsg.setPatientId(patientId + "");

        }
        chatMsg.setToUid(targetUid);
        chatMsg.setMsg(msg);
        chatMsg.setCreateTime(date);
        chatMsg.setCanceled(0);
        chatMsg.setPushed(0);
        chatMsg.setReadStatus(0);
        chatMsg.setStr1(keyId + "");
        chatMsg.setChatUserId(chatUserId);
        chatMsg.setReadUserIds(fromUserId + "");
        chatMsgService.save(chatMsg);
        return chatMsg;
    }

    private void sendNotic(ChatMsg chatMsg, Integer fromUserId,
                           String patientId, List<String> allUserIds, Integer chatUserId) {

        String name = "";
        if (!StringUtils.isEmpty(patientId)) {
            PatientUser patientUser = patientUserService.getById(patientId);
            name = patientUser.getName();
        }
        for (String userId : allUserIds) {
            String replace = userId.replace("[", "");
            userId = replace.replace("]", "");
            userId = userId.trim();
            if (!userId.equals(fromUserId + "")) {

                Channel targetUserChannel = UserChannelManager.getUserChannel(Integer.parseInt(userId));
                //2.向目标用户发送新消息提醒
                SocketFrameTextMessage targetUserMessage
                        = SocketFrameTextMessage.newGroupMessageTip(chatUserId, JSON.toJSONString(chatMsg));
                if (targetUserChannel != null) {
                    targetUserChannel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(targetUserMessage)));
                } else {
                    User user = userService.getById(userId);

                    if (StringUtils.isEmpty(name)) {
                        if (StringUtils.isEmpty(user.getPatientName())) {
                            name = user.getNickname();
                        } else {
                            name = user.getPatientName();
                        }

                    }
                    uniAppPushService.send("法罗适", name + ": " + chatMsg.getMsg(), userId, "");

                }
            }

        }

    }

    @GetMapping("/pageTrainRecordByXtUserId")
    public RestResponse pageTrainRecordByXtUserId(@RequestParam(value = "xtUserId", required = false) Integer xtUserId) {

        Page<TbUserTrainRecord> page = getPage();
        if (xtUserId == null) {
            xtUserId = SecurityUtils.getUser().getId();
        }
        IPage<TbUserTrainRecord> tbUserTrainRecordIPage = service.pageTrainRecordByXtUserId(page, xtUserId);
        if (tbUserTrainRecordIPage == null) {
            return RestResponse.failed("未查询到用户信息，请确认已关联用户信息");
        }
        return RestResponse.ok(tbUserTrainRecordIPage);
    }

    @GetMapping("/pageTrainRecordById")
    public RestResponse pageTrainRecordById(@RequestParam(value = "xtUserId", required = false) Integer xtUserId) {

        Page<TbUserTrainRecord> page = getPage();
        if (xtUserId == null) {
            xtUserId = SecurityUtils.getUser().getId();
        }
        IPage<TbUserTrainRecord> tbUserTrainRecordIPage = service.pageTrainRecordById(page, xtUserId);
        if (tbUserTrainRecordIPage == null) {
            return RestResponse.failed("未查询到用户信息，请确认已关联用户信息");
        }
        return RestResponse.ok(tbUserTrainRecordIPage);
    }

    /**
     * 根据手机号身份证号查询训练计划
     *
     * @return
     */
    @GetMapping("/trainRecordByPhone")
    public RestResponse trainRecordByPhone(@RequestParam(value = "phone", required = false) String phone, @RequestParam(value = "idCard", required = false) String idCard, @RequestParam(value = "xtUserId", required = false) String xtUserId) {

        return RestResponse.ok(service.trainRecordByPhone(phone, idCard, xtUserId));
    }

    /**
     * @return
     */
    @GetMapping("/getByIdCard")
    public RestResponse getByIdCard(@RequestParam(value = "idCard", required = false) String idCard) {

        List<TbTrainUser> tbTrainUsers = planUserService.list(new QueryWrapper<TbTrainUser>().lambda().eq(TbTrainUser::getIdCard, idCard));
        if (CollectionUtils.isEmpty(tbTrainUsers)) {
            return RestResponse.ok();
        }
        TbTrainUser tbTrainUser = tbTrainUsers.get(0);
        List<TbUserTrainRecord> tbUserTrainRecords = service.list(new QueryWrapper<TbUserTrainRecord>().lambda().eq(TbUserTrainRecord::getUserId, tbTrainUser.getUserId()).orderByAsc(TbUserTrainRecord::getDateStr));
        if (CollectionUtils.isEmpty(tbUserTrainRecords)) {
            tbUserTrainRecords = new ArrayList<>();
            List<TbSubPlan> list = subPlanService.list(new QueryWrapper<TbSubPlan>().lambda().eq(TbSubPlan::getUserId, tbTrainUser.getUserId()).orderByAsc(TbSubPlan::getStartDate));
            if (!CollectionUtils.isEmpty(list)) {
                TbSubPlan tbSubPlan = list.get(0);
                TbUserTrainRecord tbUserTrainRecord = new TbUserTrainRecord();
                tbUserTrainRecord.setTargetLoad(tbSubPlan.getLoad());
                tbUserTrainRecords.add(tbUserTrainRecord);

            }
        }
        if (!CollectionUtils.isEmpty(tbUserTrainRecords)) {
            List<String> Ids = new ArrayList<>();//
            tbUserTrainRecords = tbUserTrainRecords.stream().filter(// 过滤去重
                    v -> {
                        boolean flag = !Ids.contains(v.getDateStr() + "");
                        Ids.add(v.getDateStr() + "");
                        return flag;
                    }
            ).collect(Collectors.toList());
        }
        return RestResponse.ok(tbUserTrainRecords);
    }

    /**
     * 查询用户每天每天的训练记录
     *
     * @return
     */

    @GetMapping("/getTrainStepCount")
    public RestResponse getTrainStepCount(@RequestParam(value = "userId", required = false) String userId) {
        List<TbUserTrainRecord> tbUserTrainRecords = service.listTrainRecordByUid(userId);
        if (CollectionUtils.isEmpty(tbUserTrainRecords)) {
            return RestResponse.ok();
        }

        Map<String, List<TbUserTrainRecord>> tbUserTrainRecordMap = tbUserTrainRecords.stream()
                .collect(Collectors.groupingBy(TbUserTrainRecord::getDateStr));
        List<TbUserTrainRecord> records = new ArrayList<>();
        for (String key : tbUserTrainRecordMap.keySet()) {

            List<TbUserTrainRecord> records1 = tbUserTrainRecordMap.get(key);
            int count = 0;
            if (!CollectionUtils.isEmpty(records1)) {
                count = records1.size();
            }
            TbUserTrainRecord record = new TbUserTrainRecord();
            record.setDateStr(key);
            record.setCount(count);
            records.add(record);
        }
        return RestResponse.ok(records);
    }

    /**
     * 查询用户每天的训练记录 计算每天踩踏最小值 和最大值
     *
     * @return
     */

    @GetMapping("/getTrainRecord")
    public RestResponse getTrainRecord(@RequestParam(value = "idCard", required = false) String idCard, @RequestParam(required = false, value = "userId") String userId) {
        if (StringUtils.isEmpty(userId)) {
            List<TbTrainUser> list = planUserService.list(Wrappers.<TbTrainUser>lambdaQuery().eq(TbTrainUser::getIdCard, idCard));
            if (CollectionUtils.isEmpty(list)) {
                return RestResponse.ok(new ArrayList<>());
            }
            userId = list.get(0).getUserId();
        } else {
            List<TbTrainUser> list = planUserService.list(Wrappers.<TbTrainUser>lambdaQuery().eq(TbTrainUser::getXtUserId, userId));
            if (CollectionUtils.isEmpty(list)) {
                return RestResponse.ok(new ArrayList<>());
            }
            userId = list.get(0).getUserId();
        }


        List<TbUserTrainRecord> tbUserTrainRecords = service.listTrainRecordByUid(userId);
        if (CollectionUtils.isEmpty(tbUserTrainRecords)) {
            return RestResponse.ok();
        }
        List<Integer> tbUserTrainRecordIds = tbUserTrainRecords.stream().map(TbUserTrainRecord::getId)
                .collect(Collectors.toList());
        List<TbTrainData> tbTrainDatas = trainDataService.listByRecordIds(tbUserTrainRecordIds);//查询踩踏次数
        Map<Integer, List<TbTrainData>> tbTrainDataMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(tbTrainDatas)) {
            tbTrainDataMap = tbTrainDatas.stream()
                    .collect(Collectors.groupingBy(TbTrainData::getRecordId));
        }
        List<TbSubPlan> tbSubPlanList = subPlanService.list(Wrappers.<TbSubPlan>lambdaQuery().eq(TbSubPlan::getUserId, userId));//查询计划


        Map<String, List<TbUserTrainRecord>> tbUserTrainRecordMap = tbUserTrainRecords.stream()
                .collect(Collectors.groupingBy(TbUserTrainRecord::getDateStr));
        List<GetTrainRecordDTO> records = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


        for (String key : tbUserTrainRecordMap.keySet()) {
            Integer planStepCount = 0;//当天计划踩踏次数
            if (!CollectionUtils.isEmpty(tbSubPlanList)) {
                for (TbSubPlan tbSubPlan : tbSubPlanList) {
                    try {
                        Date dateStr = sdf.parse(key + " 00:00:00");
                        Date startDate = tbSubPlan.getStartDate();
                        Date endDate = tbSubPlan.getEndDate();
                        if (dateStr.before(endDate) && dateStr.after(startDate)) {
                            int trainStep = tbSubPlan.getTrainStep();
                            if (trainStep > 0) {
                                planStepCount = trainStep / 7;
                            }
                            break;
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }


                }
            }
            GetTrainRecordDTO getTrainRecordDTO = new GetTrainRecordDTO();
            List<TbUserTrainRecord> records1 = tbUserTrainRecordMap.get(key);//每天的训练记录
            int maxTargetLoad = 0;
            int miniTargetLoad = Integer.MAX_VALUE;
            int totalStepCount = 0;//总踩踏次数
            int totalTargetLoad = 0;//总负重次数
            if (!CollectionUtils.isEmpty(records1)) {
                for (TbUserTrainRecord tbUserTrainRecord : records1) {


                    List<TbTrainData> tbTrainData = tbTrainDataMap.get(tbUserTrainRecord.getId());
                    if (!CollectionUtils.isEmpty(tbTrainData)) {
                        totalStepCount = totalStepCount + tbTrainData.size();
                        for (TbTrainData tbTrainData1 : tbTrainData) {
                            totalTargetLoad = totalTargetLoad + tbTrainData1.getRealLoad();
                            if (tbTrainData1.getRealLoad() > maxTargetLoad) {
                                maxTargetLoad = tbTrainData1.getRealLoad();
                            }
                            if (tbTrainData1.getRealLoad() < miniTargetLoad) {
                                miniTargetLoad = tbTrainData1.getRealLoad();
                            }
                        }
                    }

                }
            }
            getTrainRecordDTO.setTbUserTrainRecordList(records1);
            getTrainRecordDTO.setDateStr(key);
            getTrainRecordDTO.setMaxTargetLoad(maxTargetLoad);
            getTrainRecordDTO.setMiniTargetLoad(miniTargetLoad);
            if (totalTargetLoad != 0 && totalStepCount != 0) {
                getTrainRecordDTO.setAverageTargetLoad(totalTargetLoad / totalStepCount);
            }
            getTrainRecordDTO.setTotalStepCount(totalStepCount);
            getTrainRecordDTO.setPlanStepCount(planStepCount);
            records.add(getTrainRecordDTO);
        }
        Collections.sort(records);
        return RestResponse.ok(records);
    }

    @Override
    protected Class<TbUserTrainRecord> getEntityClass() {
        return TbUserTrainRecord.class;
    }
}
