package cn.cuptec.faros.im;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.com.Url;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.im.bean.SocketFrameTextMessage;
import cn.cuptec.faros.im.core.SocketUser;
import cn.cuptec.faros.im.core.UserChannelManager;
import cn.cuptec.faros.im.proto.ChatProto;
import cn.cuptec.faros.service.*;
import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
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
    private UserRoleService userRoleService;
    @Resource
    private FollowUpPlanNoticeService followUpPlanNoticeService;
    @Resource
    private FollowUpPlanContentService followUpPlanContentService;
    @Resource
    private FormService formService;
    @Resource
    private ArticleService articleService;
    @Resource
    private cn.cuptec.faros.service.WxMpService wxMpService;
    @Resource
    private DoctorTeamService doctorTeamService;
    @Resource
    private DoctorPointService doctorPointService;
    @Resource
    private UpcomingService upcomingService;
    @Resource
    private PatientUserService patientUserService;
    private final Url urlData;

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
        log.info("获取聊天记录开始===============================" + param.toString());
        Integer pageNum = param.getPageNum();
        Integer pageSize = param.getPageSize();
        if (param.getMyUserId() == null) {
            param.setMyUserId(SecurityUtils.getUser().getId());
        }
        if (param.getClearTime() == null) {
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime ldt = LocalDateTime.parse("2017-09-28 01:07:05", df);
            param.setClearTime(ldt);

        }
        Integer patientId = param.getPatientId();
        LambdaQueryWrapper<ChatUser> eq = new QueryWrapper<ChatUser>().lambda().eq(ChatUser::getUid, param.getMyUserId()).eq(ChatUser::getTargetUid, param.getTargetUid());
        if (patientId != null) {
            eq.eq(ChatUser::getPatientId, patientId);
        }else {
            eq.isNull(ChatUser::getPatientId);
        }
        //查询清空历史记录
        ChatUser one = chatUserService.getOne(eq);
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
            LambdaQueryWrapper<ChatMsg> chatMsgLambdaQueryWrapper;
            if (patientId != null) {
                chatMsgLambdaQueryWrapper = Wrappers.<ChatMsg>lambdaQuery()
                        .nested(query -> query.eq(ChatMsg::getToUid, param.getMyUserId()).eq(ChatMsg::getFromUid, param.getTargetUid()).eq(ChatMsg::getPatientId, patientId).gt(ChatMsg::getCreateTime, df.format(param.getClearTime())))
                        .or(query -> query.eq(ChatMsg::getToUid, param.getTargetUid()).eq(ChatMsg::getFromUid, param.getMyUserId()).eq(ChatMsg::getPatientId, patientId).gt(ChatMsg::getCreateTime, df.format(param.getClearTime())))
                        .orderByDesc(ChatMsg::getCreateTime);
            } else {
                chatMsgLambdaQueryWrapper = Wrappers.<ChatMsg>lambdaQuery()
                        .nested(query -> query.eq(ChatMsg::getToUid, param.getMyUserId()).eq(ChatMsg::getFromUid, param.getTargetUid()).gt(ChatMsg::getCreateTime, df.format(param.getClearTime())))
                        .or(query -> query.eq(ChatMsg::getToUid, param.getTargetUid()).eq(ChatMsg::getFromUid, param.getMyUserId()).gt(ChatMsg::getCreateTime, df.format(param.getClearTime())))
                        .orderByDesc(ChatMsg::getCreateTime);
            }


            resultPage = chatMsgService.page(page, chatMsgLambdaQueryWrapper
            );
        }


        List<ChatMsg> records = resultPage.getRecords();

        if (!CollectionUtils.isEmpty(records)) {
            //查询每个人的头像昵称
            List<Integer> userIds = records.stream().map(ChatMsg::getFromUid)
                    .collect(Collectors.toList());
            List<User> users = (List<User>) userService.listByIds(userIds);
            List<UserRole> userRoles = userRoleService.list(new QueryWrapper<UserRole>().lambda().in(UserRole::getUserId, userIds));
            if (!CollectionUtils.isEmpty(userRoles)) {
                Map<Integer, List<UserRole>> roleMap = userRoles.stream()
                        .collect(Collectors.groupingBy(UserRole::getUserId));
                for (User user : users) {
                    List<UserRole> userRoles1 = roleMap.get(user.getId());
                    if (!CollectionUtils.isEmpty(userRoles1)) {
                        List<Integer> roleIds = userRoles1.stream().map(UserRole::getRoleId)
                                .collect(Collectors.toList());
                        if (roleIds.contains(20)) {
                            user.setRoleType(20);
                        }
                    }

                }

            }
            Map<Integer, User> userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, t -> t));


            List<String> otherOrderIds = new ArrayList<>();//获取图文咨询内容
            List<String> followUpPlanNoticeIds = new ArrayList<>();//随访计划
            List<String> formIds = new ArrayList<>();//表单id
            List<String> articleIds = new ArrayList<>();//表单id
            Map<Integer, PatientOtherOrder> patientOtherOrderMap = new HashMap<>();
            Map<Integer, FollowUpPlanNotice> followUpPlanNoticeMap = new HashMap<>();
            Map<Integer, Form> formMap = new HashMap<>();
            Map<Integer, Article> articleMap = new HashMap<>();
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
                if (chatMsg.getMsgType().equals(ChatProto.ARTICLE)) {//文章
                    articleIds.add(chatMsg.getStr1());
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
            //文章
            if (!CollectionUtils.isEmpty(articleIds)) {//
                List<Article> articles = (List<Article>) articleService.listByIds(articleIds);
                articleMap = articles.stream()
                        .collect(Collectors.toMap(Article::getId, t -> t));
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
                if (chatMsg.getMsgType().equals(ChatProto.ARTICLE)) {//文章

                    chatMsg.setArticle(articleMap.get(Integer.parseInt(chatMsg.getStr1())));
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
    public RestResponse readMsg(@RequestParam(value = "chatUSerId", required = false) String chatUserId,
                                @RequestParam(value = "targetUid", required = false) Integer targetUid
            , @RequestParam(value = "myUserId", required = false) Integer myUserId) {
        myUserId = SecurityUtils.getUser().getId();
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
        patientOtherOrder.setStartTime(LocalDateTime.now());
        patientOtherOrder.setEndTime(LocalDateTime.now().plusHours(24));
        patientOtherOrderService.updateById(patientOtherOrder);
        ChatUser byId = chatUserService.getById(chatUserId);
        LambdaUpdateWrapper<ChatMsg> set1 = Wrappers.<ChatMsg>lambdaUpdate()
                .eq(ChatMsg::getStr1, patientOtherOrder.getId())
                .eq(ChatMsg::getMsgType, ChatProto.PIC_CONSULTATION)
                .set(ChatMsg::getStr2, str2);
        chatMsgService.update(set1);

        UserServicePackageInfo userServicePackageInfo = userServicePackageInfoService.getById(patientOtherOrder.getUserServiceId());

        if (str2.equals("1")) {


            //添加医生积分 判断是否是抢单模式
            if (patientOtherOrder.getDoctorTeamId() != null && patientOtherOrder.getAmount() != null) {
                Integer doctorTeamId = patientOtherOrder.getDoctorTeamId();
                DoctorTeam doctorTeam = doctorTeamService.getById(doctorTeamId);
                if (doctorTeam.getModel().equals(1)) {
                    DoctorPoint doctorPoint = new DoctorPoint();
                    doctorPoint.setPoint(patientOtherOrder.getAmount());
                    doctorPoint.setDoctorUserId(SecurityUtils.getUser().getId());
                    doctorPoint.setPointDesc("图文咨询");
                    doctorPoint.setWithdrawStatus(1);
                    doctorPoint.setPatientId(patientOtherOrder.getUserId());
                    doctorPoint.setCreateTime(LocalDateTime.now());
                    doctorPoint.setOrderNo(patientOtherOrder.getOrderNo());
                    doctorPointService.save(doctorPoint);

                }
            }


            //接收
            if (userServicePackageInfo != null) {
                userServicePackageInfo.setUseCount(userServicePackageInfo.getUseCount() + 1);
                userServicePackageInfoService.updateById(userServicePackageInfo);


            }

            if (byId.getGroupType().equals(0)) {
                ChatUser fromUserChat = new ChatUser();
                fromUserChat.setUid(byId.getUid());
                fromUserChat.setTargetUid(byId.getTargetUid());


                ChatUser toUserChat = new ChatUser();
                toUserChat.setUid(byId.getTargetUid());
                toUserChat.setTargetUid(byId.getUid());

                List<ChatUser> chatUsers = new ArrayList<>();
                chatUsers.add(fromUserChat);
                chatUsers.add(toUserChat);

                chatUsers.forEach(c -> {
                    LambdaQueryWrapper<ChatUser> eq = Wrappers.<ChatUser>lambdaQuery().eq(ChatUser::getTargetUid, c.getTargetUid()).eq(ChatUser::getUid, c.getUid());
                    if (patientOtherOrder.getPatientId() != null) {
                        eq.eq(ChatUser::getPatientId, patientOtherOrder.getPatientId());
                    }else {
                        eq.isNull(ChatUser::getPatientId);
                    }
                    ChatUser one = chatUserService.getOne(eq);
                    if (one != null) {
                        User byId1 = userService.getById(c.getUid());
                        Integer receiverId = 0;
                        if (!StringUtils.isEmpty(byId1.getPassword())) {
                            //代表是医生
                            receiverId = c.getUid();
                        }

                        if (patientOtherOrder.getPatientId() != null) {
                            LambdaUpdateWrapper<ChatUser> set = Wrappers.<ChatUser>lambdaUpdate()
                                    .eq(ChatUser::getUid, c.getUid())
                                    .eq(ChatUser::getPatientId, patientOtherOrder.getPatientId())
                                    .eq(ChatUser::getTargetUid, c.getTargetUid())
                                    .set(ChatUser::getPatientOtherOrderStatus, str2)
                                    .set(ChatUser::getReceiverId, receiverId)
                                    .set(ChatUser::getServiceEndTime, LocalDateTime.now().plusHours(24))
                                    .set(ChatUser::getServiceStartTime, LocalDateTime.now());
                            chatUserService.update(set
                            );
                        } else {
                            LambdaUpdateWrapper<ChatUser> set = Wrappers.<ChatUser>lambdaUpdate()
                                    .eq(ChatUser::getUid, c.getUid())
                                    .isNull(ChatUser::getPatientId)
                                    .eq(ChatUser::getTargetUid, c.getTargetUid())
                                    .set(ChatUser::getPatientOtherOrderStatus, str2)
                                    .set(ChatUser::getReceiverId, receiverId)
                                    .set(ChatUser::getServiceEndTime, LocalDateTime.now().plusHours(24))
                                    .set(ChatUser::getServiceStartTime, LocalDateTime.now());
                            chatUserService.update(set
                            );
                        }

                    }
                });


            } else {
                updateChatUser.setServiceStartTime(LocalDateTime.now());
                updateChatUser.setPatientOtherOrderStatus(str2);
                updateChatUser.setReceiverId(SecurityUtils.getUser().getId());
                updateChatUser.setServiceEndTime(LocalDateTime.now().plusHours(24));
                chatUserService.updateById(updateChatUser);
            }
        } else {//拒绝
            //退款
            if (patientOtherOrder.getAmount() != null) {
                Dept dept = deptService.getById(patientOtherOrder.getDeptId());
                String url = urlData.getRefundUrl() + "?orderNo=" + patientOtherOrder.getOrderNo() + "&transactionId=" + patientOtherOrder.getTransactionId() + "&subMchId=" + dept.getSubMchId() + "&totalFee=" + new BigDecimal(patientOtherOrder.getAmount()).multiply(new BigDecimal(100)).intValue() + "&refundFee=" + new BigDecimal(patientOtherOrder.getAmount()).multiply(new BigDecimal(100)).intValue();
                String result = HttpUtil.get(url);
            }
            if (byId.getGroupType().equals(0)) {
                ChatUser fromUserChat = new ChatUser();
                fromUserChat.setUid(byId.getUid());
                fromUserChat.setTargetUid(byId.getTargetUid());


                ChatUser toUserChat = new ChatUser();
                toUserChat.setUid(byId.getTargetUid());
                toUserChat.setTargetUid(byId.getUid());

                List<ChatUser> chatUsers = new ArrayList<>();
                chatUsers.add(fromUserChat);
                chatUsers.add(toUserChat);

                chatUsers.forEach(c -> {
                    LambdaQueryWrapper<ChatUser> eq = Wrappers.<ChatUser>lambdaQuery().eq(ChatUser::getTargetUid, c.getTargetUid()).eq(ChatUser::getUid, c.getUid());
                    if (patientOtherOrder.getPatientId() != null) {
                        eq.eq(ChatUser::getPatientId, patientOtherOrder.getPatientId());
                    }else {
                        eq.isNull(ChatUser::getPatientId);
                    }
                    ChatUser one = chatUserService.getOne(eq);
                    if (one != null) {


                        if (patientOtherOrder.getPatientId() != null) {
                            LambdaUpdateWrapper<ChatUser> set = Wrappers.<ChatUser>lambdaUpdate()
                                    .eq(ChatUser::getUid, c.getUid())
                                    .eq(ChatUser::getPatientId, patientOtherOrder.getPatientId())
                                    .eq(ChatUser::getTargetUid, c.getTargetUid())
                                    .set(ChatUser::getPatientOtherOrderStatus, str2);

                            chatUserService.update(
                                    set
                            );
                        } else {
                            LambdaUpdateWrapper<ChatUser> set = Wrappers.<ChatUser>lambdaUpdate()
                                    .eq(ChatUser::getUid, c.getUid())
                                    .isNull(ChatUser::getPatientId)
                                    .eq(ChatUser::getTargetUid, c.getTargetUid())
                                    .set(ChatUser::getPatientOtherOrderStatus, str2);
                            chatUserService.update(
                                    set
                            );
                        }

                    }
                });


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
        String name = "";
        if (StringUtils.isEmpty(user.getPatientName())) {
            name = user.getNickname();
        } else {
            name = user.getPatientName();
        }
        if (patientOtherOrder.getPatientId() != null) {
            PatientUser patientUser = patientUserService.getById(patientOtherOrder.getPatientId());
            name = patientUser.getName();
        }
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String time = df.format(now);
        wxMpService.sendDoctorTip(user.getMpOpenId(), "您有新的医生消息", name, time, msg, "/pages/news/news");
        //修改代办事项状态
        upcomingService.update(Wrappers.<Upcoming>lambdaUpdate()
                .eq(Upcoming::getOrderId, patientOtherOrder.getId())
                .set(Upcoming::getRedStatus, 1)

        );
        return RestResponse.ok();
    }

    /**
     * 查询 聊天的所有图片
     */
    @GetMapping("/queryAllMsgIMage")
    public RestResponse queryAllMsgIMage(
            @RequestParam("msgId") Integer msgId) {
        ChatMsg chatMsg = chatMsgService.getById(msgId);
        List<ChatMsg> chatMsgs = new ArrayList<>();
        List<String> images = new ArrayList<>();
        if (chatMsg == null) {
            return RestResponse.ok(images);
        }
        if (chatMsg.getChatUserId() == null) {
            //单聊
            chatMsgs = chatMsgService.list(Wrappers.<ChatMsg>lambdaQuery()
                    .nested(query -> query.eq(ChatMsg::getToUid, chatMsg.getFromUid()).eq(ChatMsg::getFromUid, chatMsg.getToUid()).eq(ChatMsg::getMsgType, ChatProto.MESSAGE_PIC))
                    .or(query -> query.eq(ChatMsg::getToUid, chatMsg.getToUid()).eq(ChatMsg::getFromUid, chatMsg.getFromUid()).eq(ChatMsg::getMsgType, ChatProto.MESSAGE_PIC))
                    .orderByDesc(ChatMsg::getCreateTime)
            );
        } else {
            //群聊
            //查询群聊
            chatMsgs = chatMsgService.list(Wrappers.<ChatMsg>lambdaQuery()
                    .nested(query -> query.eq(ChatMsg::getChatUserId, chatMsg.getChatUserId()).eq(ChatMsg::getMsgType, ChatProto.MESSAGE_PIC))
                    .orderByDesc(ChatMsg::getCreateTime));
        }

        if (!CollectionUtils.isEmpty(chatMsgs)) {
            images = chatMsgs.stream().map(ChatMsg::getUrl)
                    .collect(Collectors.toList());
        }
        return RestResponse.ok(images);
    }

}
