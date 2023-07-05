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
    private DoctorTeamPeopleService doctorTeamPeopleService;
    @Resource
    private SysTemNoticService sysTemNoticService;

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
                    String userId = tbTrainUser.getUserId();
                    String idCard = tbTrainUser.getIdCard();
                    Integer xtUserId = tbTrainUser.getXtUserId();
                    List<PatientUser> patientUsers = patientUserService.list(new QueryWrapper<PatientUser>().lambda().eq(PatientUser::getIdCard, idCard));
                    String patientId = null;
                    if (!CollectionUtils.isEmpty(patientUsers)) {
                        PatientUser patientUser = patientUsers.get(0);
                        patientId = patientUser.getId();
                        xtUserId = patientUser.getUserId();
                    }
                    List<DoctorTeamPeople> doctorTeamPeopleList = doctorTeamPeopleService.list(new QueryWrapper<DoctorTeamPeople>().lambda()
                            .eq(DoctorTeamPeople::getTeamId, tbTrainUser.getDoctorTeamId()));
                    List<Integer> doctorIds = doctorTeamPeopleList.stream().map(DoctorTeamPeople::getUserId)
                            .collect(Collectors.toList());
                    for (TbUserTrainRecord tbUserTrainRecord : userTrainRecordList) {
                        Long keyId = tbUserTrainRecord.getKeyId();
                        Integer painLevel = tbUserTrainRecord.getPainLevel();//vas值
                        if (painLevel != null) {
                            if (painLevel > 2) {
                                //发送信息String stockUserName,String keyId,String stockUserId,Integer xtUserId,String msg,List<Integer> doctorIds,Integer teamId
                                push(tbUserTrainRecord.getDateStr(), tbUserTrainRecord.getFrequency() + "", patientId, 1, tbTrainUser.getName(), keyId + "", userId, xtUserId, "训练VAS值超过预期", doctorIds, tbTrainUser.getDoctorTeamId());
                            }
                        }
                        String adverseReactions = tbUserTrainRecord.getAdverseReactions();//异常反馈
                        if (adverseReactions != null) {
                            if (!StringUtils.isEmpty(adverseReactions)) {
                                //发送信息
                                push(tbUserTrainRecord.getDateStr(), tbUserTrainRecord.getFrequency() + "", patientId, 2, tbTrainUser.getName(), keyId + "", userId, xtUserId, "异常反馈(" + adverseReactions + ")", doctorIds, tbTrainUser.getDoctorTeamId());

                            }

                        }
                        Integer successTime = tbUserTrainRecord.getSuccessTime();
                        Integer warningTime = tbUserTrainRecord.getWarningTime();
                        Integer time = 0;
                        if (warningTime != null) {
                            time = time + warningTime;
                        }
                        if (successTime != null) {
                            time = time + successTime;
                        }
                        if (time != 0 && tbUserTrainRecord.getTotalTrainStep() != null
                                && time < tbUserTrainRecord.getTotalTrainStep() / 2) {
                            //发送信息
                            push(tbUserTrainRecord.getDateStr(), tbUserTrainRecord.getFrequency() + "", patientId, 3, tbTrainUser.getName(), keyId + "", userId, xtUserId, "踩踏次数异常", doctorIds, tbTrainUser.getDoctorTeamId());

                        }
                    }

                }
            }
        });
    }

    private void push(String date_str, String frequency, String patientId, Integer type, String stockUserName, String keyId, String stockUserId, Integer xtUserId, String msg, List<Integer> doctorIds, Integer teamId) {
        List<SysTemNotic> sysTemNotics = sysTemNoticService.list(new QueryWrapper<SysTemNotic>().lambda()
                .eq(SysTemNotic::getKeyId, keyId)
                .eq(SysTemNotic::getKeyIdType, type));
        if (!CollectionUtils.isEmpty(sysTemNotics)) {
            return;
        }

        List<ChatUser> chatUsers = chatUserService.list(new QueryWrapper<ChatUser>().lambda()
                .eq(ChatUser::getTeamId, teamId)
                .eq(ChatUser::getPatientId, patientId)
                .eq(ChatUser::getTargetUid, xtUserId));
        Integer chatUserId = null;
        if (!CollectionUtils.isEmpty(chatUsers)) {
            chatUserId = chatUsers.get(0).getId();
        }
        saveSystemNotic(date_str, frequency, type, xtUserId, keyId, msg, stockUserId, chatUserId, teamId);

        for (Integer doctorId : doctorIds) {
            //发送消息

            Channel targetUserChannel = UserChannelManager.getUserChannel(doctorId);
            //2.向目标用户发送新消息提醒
            SocketFrameTextMessage targetUserMessage
                    = SocketFrameTextMessage.newMessageTip(xtUserId, "", "", new Date(), ChatProto.SYSTEM_NOTIC, "");

            if (targetUserChannel != null) {
                targetUserChannel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(targetUserMessage)));
            } else {


                uniAppPushService.send("法罗适", stockUserName + ": " + msg, doctorId + "", "");

            }
        }
    }


    public void saveSystemNotic(String date_str, String frequency, Integer keyIdType, Integer xtUserId, String keyId, String msg, String stockUserId, Integer chatUserId, Integer teamId) {
        SysTemNotic sysTemNotic = new SysTemNotic();
        sysTemNotic.setDateStr(date_str);
        sysTemNotic.setFrequency(frequency);
        sysTemNotic.setKeyIdType(keyIdType);
        sysTemNotic.setCreateTime(LocalDateTime.now());
        sysTemNotic.setContent(msg);
        sysTemNotic.setTitle(msg);
        sysTemNotic.setTeamId(teamId);
        sysTemNotic.setReadStatus(1);
        sysTemNotic.setType(1);
        sysTemNotic.setPatientUserId(xtUserId + "");
        sysTemNotic.setStockUserId(stockUserId);
        sysTemNotic.setKeyId(keyId + "");
        sysTemNotic.setChatUserId(chatUserId);
        sysTemNoticService.save(sysTemNotic);
    }

    @GetMapping("/getByUserIdAndFrequency")
    public RestResponse pageTrainRecordByXtUserId(@RequestParam(value = "frequency", required = false) String frequency,
                                                  @RequestParam(value = "userId", required = false) String userId,
                                                  @RequestParam(value = "dateStr", required = false) String dateStr) {


        List<TbUserTrainRecord> tbUserTrainRecordIPage = service.list(new QueryWrapper<TbUserTrainRecord>().lambda().eq(TbUserTrainRecord::getUserId, userId)
                .eq(TbUserTrainRecord::getFrequency, frequency)
                .eq(TbUserTrainRecord::getDateStr, dateStr));
        if (!CollectionUtils.isEmpty(tbUserTrainRecordIPage)) {
            List<TbTrainData> list = trainDataService.list(new QueryWrapper<TbTrainData>().lambda().eq(TbTrainData::getUserId, userId)
                    .eq(TbTrainData::getFrequency, Integer.parseInt(frequency) - 1)
                    .eq(TbTrainData::getDateStr, dateStr));
            tbUserTrainRecordIPage.get(0).setTrainDataList(list);
        }
        return RestResponse.ok(tbUserTrainRecordIPage);
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
