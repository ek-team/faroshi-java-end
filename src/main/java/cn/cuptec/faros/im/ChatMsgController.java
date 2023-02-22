package cn.cuptec.faros.im;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.im.bean.SocketFrameTextMessage;
import cn.cuptec.faros.im.core.SocketUser;
import cn.cuptec.faros.im.core.UserChannelManager;
import cn.cuptec.faros.im.proto.ChatProto;
import cn.cuptec.faros.service.*;
import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@RestController
@RequestMapping("/chatMsg")
public class ChatMsgController {
    @Resource
    private ChatMsgService chatMsgService;
    @Resource
    private PatientOtherOrderService patientOtherOrderService;//患者其它订单
    @Resource
    private UserServicePackageInfoService userServicePackageInfoService;
    @Resource
    private ChatUserService chatUserService;
    @Resource
    private DeptService deptService;
    @Resource
    private UserService userService;
    @Resource
    private FollowUpPlanNoticeService followUpPlanNoticeService;
    @Resource
    private FollowUpPlanContentService followUpPlanContentService;
    @Resource
    private FormService formService;
    @Resource
    private cn.cuptec.faros.service.WxMpService wxMpService;

    @ApiOperation(value = "查询历史记录")
    @GetMapping("/testRead")
    public void tesetRead(@RequestParam("chatUserId") Integer chatUserId) {
        List<ChatMsg> chatMsgs = chatMsgService.list(new QueryWrapper<ChatMsg>().lambda().eq(ChatMsg::getChatUserId, chatUserId));
        if (!CollectionUtils.isEmpty(chatMsgs)) {
            List<ChatMsg> updateChatMsg = new ArrayList<>();
            for (ChatMsg chatMsg : chatMsgs) {
                String readUserIds = chatMsg.getReadUserIds();
                if (StringUtils.isEmpty(readUserIds)) {
                    readUserIds = 2277 + "";
                    chatMsg.setReadUserIds(readUserIds);
                    updateChatMsg.add(chatMsg);
                } else {
                    if (readUserIds.indexOf("2277") <= 0) {
                        readUserIds = readUserIds + "," + 2277;
                        chatMsg.setReadUserIds(readUserIds);
                        updateChatMsg.add(chatMsg);
                    }
                }

            }
            if (!CollectionUtils.isEmpty(updateChatMsg)) {
                chatMsgService.updateBatchById(updateChatMsg);
            }
        }
    }

    @ApiOperation(value = "查询历史记录")
    @PostMapping("/queryChatMsgHistory")
    public RestResponse queryChatMsgHistory(@RequestBody SocketFrameTextMessage param) {
        log.info("获取聊天记录开始===============================");
        Integer pageNum = param.getPageNum();
        Integer pageSize = param.getPageSize();
        param.setMyUserId(SecurityUtils.getUser().getId());
        if (param.getClearTime() == null) {
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime ldt = LocalDateTime.parse("2017-09-28 01:07:05", df);
            param.setClearTime(ldt);

        }
        //查询清空历史记录
        ChatUser one = chatUserService.getOne(new QueryWrapper<ChatUser>().lambda().eq(ChatUser::getUid, param.getMyUserId()).eq(ChatUser::getTargetUid, param.getTargetUid()));
        if (one != null) {
            if (one.getClearTime() != null) {
                param.setClearTime(one.getClearTime());
            }
        }
        IPage page = new Page(pageNum, pageSize);
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        IPage resultPage;
        if (param.getChatUserId() != null) {
            //查询群聊
            resultPage = chatMsgService.page(page, Wrappers.<ChatMsg>lambdaQuery()
                    .nested(query -> query.eq(ChatMsg::getChatUserId, param.getChatUserId()).gt(ChatMsg::getCreateTime, df.format(param.getClearTime())))
                    .orderByDesc(ChatMsg::getCreateTime));
        } else {
            resultPage = chatMsgService.page(page, Wrappers.<ChatMsg>lambdaQuery()
                    .nested(query -> query.eq(ChatMsg::getToUid, param.getMyUserId()).eq(ChatMsg::getFromUid, param.getTargetUid()).gt(ChatMsg::getCreateTime, df.format(param.getClearTime())))
                    .or(query -> query.eq(ChatMsg::getToUid, param.getTargetUid()).eq(ChatMsg::getFromUid, param.getMyUserId()).gt(ChatMsg::getCreateTime, df.format(param.getClearTime())))
                    .orderByDesc(ChatMsg::getCreateTime)
            );
        }


        List<ChatMsg> records = resultPage.getRecords();

        if (!CollectionUtils.isEmpty(records)) {
            //查询每个人的头像昵称
            List<Integer> userIds = records.stream().map(ChatMsg::getFromUid)
                    .collect(Collectors.toList());
            List<User> users = (List<User>) userService.listByIds(userIds);
            Map<Integer, User> userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, t -> t));


            List<String> otherOrderIds = new ArrayList<>();//获取图文咨询内容
            List<String> followUpPlanNoticeIds = new ArrayList<>();//随访计划
            List<String> formIds = new ArrayList<>();//表单id
            Map<Integer, PatientOtherOrder> patientOtherOrderMap = new HashMap<>();
            Map<Integer, FollowUpPlanNotice> followUpPlanNoticeMap = new HashMap<>();
            Map<Integer, Form> formMap = new HashMap<>();
            for (ChatMsg chatMsg : records) {
                chatMsg.setUser(userMap.get(chatMsg.getFromUid()));
                if (chatMsg.getMsgType().equals(ChatProto.PIC_CONSULTATION)) {//图文咨询
                    otherOrderIds.add(chatMsg.getStr1());
                }
                if (chatMsg.getMsgType().equals(ChatProto.FOLLOW_UP_PLAN)) {//随访计划
                    followUpPlanNoticeIds.add(chatMsg.getStr1());
                }
                if (chatMsg.getMsgType().equals(ChatProto.FORM)) {//表单
                    formIds.add(chatMsg.getStr1());
                }
            }
            if (!CollectionUtils.isEmpty(otherOrderIds)) {//图文咨询
                List<PatientOtherOrder> patientOtherOrders = (List<PatientOtherOrder>) patientOtherOrderService.listByIds(otherOrderIds);
                if (!CollectionUtils.isEmpty(patientOtherOrders)) {
                    for (PatientOtherOrder patientOtherOrder : patientOtherOrders) {
                        String imageUrl = patientOtherOrder.getImageUrl();
                        if (!StringUtils.isEmpty(imageUrl)) {
                            String[] split = imageUrl.split(",");
                            List<String> strings = Arrays.asList(split);
                            patientOtherOrder.setImageUrlList(strings);

                        } else {
                            patientOtherOrder.setImageUrlList(new ArrayList<>());
                        }
                    }
                }
                patientOtherOrderMap = patientOtherOrders.stream()
                        .collect(Collectors.toMap(PatientOtherOrder::getId, t -> t));
            }
            if (!CollectionUtils.isEmpty(followUpPlanNoticeIds)) {//随访计划
                List<FollowUpPlanNotice> followUpPlanNoticeList = (List<FollowUpPlanNotice>) followUpPlanNoticeService.listByIds(followUpPlanNoticeIds);
                if (!CollectionUtils.isEmpty(followUpPlanNoticeList)) {
                    //查询推送内容
                    List<Integer> followUpPlanContentIds = followUpPlanNoticeList.stream().map(FollowUpPlanNotice::getFollowUpPlanContentId)
                            .collect(Collectors.toList());
                    List<FollowUpPlanContent> followUpPlanContents = (List<FollowUpPlanContent>) followUpPlanContentService.listByIds(followUpPlanContentIds);
                    if (!CollectionUtils.isEmpty(followUpPlanContents)) {
                        List<Integer> formIdList = new ArrayList<>();
                        for (FollowUpPlanContent followUpPlanContent : followUpPlanContents) {
                            if (followUpPlanContent.getFormId() != null) {
                                formIdList.add(followUpPlanContent.getFormId());
                            }
                        }
                        if (!CollectionUtils.isEmpty(formIdList)) {
                            List<Form> forms = (List<Form>) formService.listByIds(formIdList);
                            Map<Integer, Form> formMap1 = forms.stream()
                                    .collect(Collectors.toMap(Form::getId, t -> t));
                            for (FollowUpPlanContent followUpPlanContent : followUpPlanContents) {
                                if (followUpPlanContent.getFormId() != null) {
                                    Form form = formMap1.get(followUpPlanContent.getFormId());
                                    if (form != null) {
                                        followUpPlanContent.setForm(form);
                                    }
                                }
                            }
                        }
                        Map<Integer, FollowUpPlanContent> followUpPlanContentMap = followUpPlanContents.stream()
                                .collect(Collectors.toMap(FollowUpPlanContent::getId, t -> t));
                        for (FollowUpPlanNotice followUpPlanNotice : followUpPlanNoticeList) {
                            followUpPlanNotice.setFollowUpPlanContent(followUpPlanContentMap.get(followUpPlanNotice.getFollowUpPlanContentId()));
                        }
                    }


                }

                followUpPlanNoticeMap = followUpPlanNoticeList.stream()
                        .collect(Collectors.toMap(FollowUpPlanNotice::getId, t -> t));
            }
            if (!CollectionUtils.isEmpty(formIds)) {//
                List<Form> forms = (List<Form>) formService.listByIds(formIds);
                formMap = forms.stream()
                        .collect(Collectors.toMap(Form::getId, t -> t));
            }
            for (ChatMsg chatMsg : records) {
                if (chatMsg.getMsgType().equals(ChatProto.PIC_CONSULTATION)) {//图文咨询
                    PatientOtherOrder patientOtherOrder = patientOtherOrderMap.get(Integer.parseInt(chatMsg.getStr1()));

                    chatMsg.setPatientOtherOrder(patientOtherOrder);
                }

                if (chatMsg.getMsgType().equals(ChatProto.FOLLOW_UP_PLAN)) {//随访计划
                    FollowUpPlanNotice followUpPlanNotice = followUpPlanNoticeMap.get(Integer.parseInt(chatMsg.getStr1()));

                    chatMsg.setFollowUpPlanNotice(followUpPlanNotice);
                }
                if (chatMsg.getMsgType().equals(ChatProto.FORM)) {//表单

                    chatMsg.setForm(formMap.get(Integer.parseInt(chatMsg.getStr1())));
                }
            }

            resultPage.setRecords(records);
        }

        log.info("获取聊天记录结束===============================");
        return RestResponse.ok(resultPage);
    }

    /**
     *
     */
    @GetMapping("/readMsg")
    public RestResponse readMsg(@RequestParam(value = "chatUSerId",required = false) String chatUserId,
                                @RequestParam(value = "targetUid",required = false) Integer targetUid
            , @RequestParam(value = "myUserId",required = false) Integer myUserId) {

        if (StringUtils.isEmpty(chatUserId)) {

            chatMsgService.setReaded(myUserId, targetUid);

        } else {
            List<ChatMsg> chatMsgs = chatMsgService.list(new QueryWrapper<ChatMsg>().lambda()
                    .eq(ChatMsg::getChatUserId, chatUserId)
                    .notLike(ChatMsg::getReadUserIds, myUserId));
            if (!CollectionUtils.isEmpty(chatMsgs)) {
                List<ChatMsg> updateChatMsg = new ArrayList<>();
                for (ChatMsg chatMsg : chatMsgs) {
                    String readUserIds = chatMsg.getReadUserIds();
                    if (StringUtils.isEmpty(readUserIds)) {
                        readUserIds = myUserId + "";
                        chatMsg.setReadUserIds(readUserIds);
                        updateChatMsg.add(chatMsg);
                    } else {
                        if (readUserIds.indexOf(myUserId + "") < 0) {
                            readUserIds = readUserIds + "," + myUserId;
                            chatMsg.setReadUserIds(readUserIds);
                            updateChatMsg.add(chatMsg);
                        }
                    }

                }
                if (!CollectionUtils.isEmpty(updateChatMsg)) {
                    chatMsgService.updateBatchById(updateChatMsg);
                }
            }
        }
        return RestResponse.ok();
    }

    /**
     * 医生接受或者拒绝图文咨询
     * str2 0-待接收 1-接收 2-拒绝
     */
    @GetMapping("/receiverPicConsultation")
    public RestResponse receiverPicConsultation(
            @RequestParam("str2") String str2,
            @RequestParam(value = "patientOtherOrderId", required = false) Integer patientOtherOrderId) {

        PatientOtherOrder patientOtherOrder = patientOtherOrderService.getOne(new QueryWrapper<PatientOtherOrder>().lambda()
                .eq(PatientOtherOrder::getId, patientOtherOrderId));
        Integer chatUserId = patientOtherOrder.getChatUserId();
        ChatUser updateChatUser = new ChatUser();
        updateChatUser.setId(chatUserId);
        updateChatUser.setPatientOtherOrderStatus(str2);
        chatUserService.updateById(updateChatUser);
        patientOtherOrder.setAcceptStatus(str2);
        patientOtherOrderService.updateById(patientOtherOrder);
        ChatUser byId = chatUserService.getById(chatUserId);
        ChatMsg chatMsg = new ChatMsg();
        chatMsg.setId(byId.getMsgId());
        chatMsg.setStr2(str2);
        chatMsgService.updateById(chatMsg);
        UserServicePackageInfo userServicePackageInfo = userServicePackageInfoService.getById(patientOtherOrder.getUserServiceId());

        if (str2.equals("1")) {
            //接收
            if (userServicePackageInfo != null) {
                userServicePackageInfo.setUseCount(userServicePackageInfo.getUseCount() + 1);
                userServicePackageInfoService.updateById(userServicePackageInfo);

                updateChatUser.setServiceStartTime(LocalDateTime.now());
                updateChatUser.setServiceEndTime(LocalDateTime.now().plusHours(24));
                chatUserService.updateById(updateChatUser);
            } else {
                updateChatUser.setServiceStartTime(LocalDateTime.now());
                updateChatUser.setServiceEndTime(LocalDateTime.now().plusHours(patientOtherOrder.getHour()));
                chatUserService.updateById(updateChatUser);
            }
        } else {//拒绝
            //退款
            if (patientOtherOrder.getAmount() != null) {
                Dept dept = deptService.getById(patientOtherOrder.getDeptId());
                String url = "https://api.redadzukibeans.com/weChat/wxpay/otherRefundOrder?orderNo=" + patientOtherOrder.getOrderNo() + "&transactionId=" + patientOtherOrder.getTransactionId() + "&subMchId=" + dept.getSubMchId() + "&totalFee=" + new BigDecimal(patientOtherOrder.getAmount()).multiply(new BigDecimal(100)).intValue() + "&refundFee=" + new BigDecimal(patientOtherOrder.getAmount()).multiply(new BigDecimal(100)).intValue();
                String result = HttpUtil.get(url);
            }


        }
        User user = userService.getById(patientOtherOrder.getUserId());
        //发送微信模板通知用户 接受状态
        String msg = "";
        if (str2.equals("1")) {
            msg = "医生已接收您的咨询";
        } else {
            msg = "医生已拒绝您的咨询";
        }
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String time = df.format(now);
        wxMpService.sendDoctorTip(user.getMpOpenId(), "您有新的医生消息", "", time, msg, "/pages/news/news");

        return RestResponse.ok();
    }
}
