package cn.cuptec.faros.im.handler.base;

import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.im.bean.SocketFrameTextMessage;
import cn.cuptec.faros.im.core.SocketUser;
import cn.cuptec.faros.im.core.UserChannelManager;
import cn.cuptec.faros.im.proto.ChatProto;
import cn.cuptec.faros.service.*;
import cn.cuptec.faros.util.ThreadPoolExecutorFactory;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.google.common.collect.Lists;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

@Slf4j
public abstract class AbstractP2PMessageHandler extends AbstractMessageHandler {

    @Resource
    private ChatMsgService chatMsgService;
    @Resource
    private ChatUserService chatUserService;
    @Resource
    private DoctorTeamPeopleService doctorTeamPeopleService;
    @Resource
    private UniAppPushService uniAppPushService;
    @Resource
    private PatientUserService patientUserService;
    @Resource
    private cn.cuptec.faros.service.WxMpService wxMpService;
    @Resource
    private UserService userService;
    @Resource
    private FormService formService;
    @Resource
    private UserFollowDoctorService userFollowDoctorService;
    @Resource
    private PatientOtherOrderService patientOtherOrderService;
    @Resource
    private UserGroupRelationUserService userGroupRelationUserService;
    @Resource
    private ArticleService articleService;
    @Resource
    private UserRoleService userRoleService;

    @Override
    @Transactional
    public void handle(Channel channel, SocketFrameTextMessage origionMessage) {

        try {
            Date createTime = new Date();
            //
            SocketUser userInfo = UserChannelManager.getUserInfo(channel);
            User fromUser = userInfo.getUserInfo();
            log.info("kkk" + fromUser.toString());
            User byId2 = userService.getById(fromUser.getId());
            if (!StringUtils.isEmpty(byId2.getNickname())) {
                fromUser.setPatientName(byId2.getNickname());
            }
            if (fromUser.getId() == null) {
                channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(SocketFrameTextMessage.authRequired())));
                return;
            }
            if (origionMessage.getMsgType().equals(ChatProto.MESSAGE_PIC)) {
                origionMessage.setMsg("[图片]");
            }
            //保存新消息到记录中
            if (origionMessage.getMsgType().equals(ChatProto.PIC_CONSULTATION)) {
                origionMessage.setMsg("图文咨询");
                origionMessage.setStr2("0");//0-待接收 1-接收 2-拒绝
            }
            ChatMsg chatMsg = saveChatMsg(origionMessage, fromUser, false, new Date());
            if (origionMessage.getMsgType().equals(ChatProto.FORM)) {
                origionMessage.setMsg("表单");
                String formId = origionMessage.getStr1();
                Form form = formService.getById(formId);
                chatMsg.setForm(form);

            }
            chatMsg.setMsg(origionMessage.getMsg());
            User byId1 = userService.getById(chatMsg.getFromUid());
            List<UserRole> userRoles = userRoleService.list(new QueryWrapper<UserRole>().lambda().eq(UserRole::getUserId, byId1.getId()));
            if (!org.springframework.util.CollectionUtils.isEmpty(userRoles)) {
                List<Integer> roleIds = userRoles.stream().map(UserRole::getRoleId)
                        .collect(Collectors.toList());
                if (roleIds.contains(20)) {
                    byId1.setRoleType(20);
                }

            }
            chatMsg.setUser(byId1);

            if (origionMessage.getMsgType().equals(ChatProto.ARTICLE)) {
                origionMessage.setMsg("文章");
                Article article = articleService.getById(origionMessage.getStr1());
                chatMsg.setArticle(article);
            }
            if (origionMessage.getMsgType().equals(ChatProto.FOLLOW_UP_PLAN)) {
                origionMessage.setMsg("随访计划");
            }
            if (origionMessage.getMsgType().equals(ChatProto.VIDEO)) {
                origionMessage.setMsg("语音消息 ");
            }
            if (origionMessage.getMsgType().equals(ChatProto.VIDEO_URL)) {
                origionMessage.setMsg("视频消息");
            }
            channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(SocketFrameTextMessage.responseMessage(chatMsg))));

            //判断是否是群聊
            if (origionMessage.getChatUserId() != null) {
                //群发消息
                ChatUser byId = chatUserService.getById(origionMessage.getChatUserId());
                log.info("团队聊天" + byId.toString());
                //推送群聊的消息给所有人
                String data = byId.getUserIds();
                List<String> allUserIds = Arrays.asList(data.split(","));
                sendNotic(chatMsg, fromUser, origionMessage, allUserIds);
                //判断是否是患者发送消息
                if (fromUser.getId().equals(byId.getTargetUid())) {
                    if (!byId.getChatCount().equals(0)) {
                        byId.setChatCount(byId.getChatCount() - 1);
                        String patientOtherOrderNo = byId.getPatientOtherOrderNo();
                        if (!StringUtils.isEmpty(patientOtherOrderNo)) {
                            PatientOtherOrder patientOtherOrder = new PatientOtherOrder();
                            patientOtherOrder.setId(Integer.parseInt(patientOtherOrderNo));
                            patientOtherOrder.setNewMsg(1);
                            patientOtherOrderService.updateById(patientOtherOrder);

                        }
                    }

                } else {
                    //医生发送消息 设置接收状态为已回
                    byId.setReceiverStatus(1);
                }
                //更新群聊 聊天时间和 最后聊天内容
                byId.setLastChatTime(new Date());
                if (origionMessage.getMsgType().equals(ChatProto.MESSAGE_PIC)) {
                    byId.setLastMsg("[图片]");
                } else {
                    byId.setLastMsg(origionMessage.getMsg());
                }

                if (origionMessage.getMsgType().equals(ChatProto.FORM)) {
                    byId.setLastMsg("表单");
                }
                if (origionMessage.getMsgType().equals(ChatProto.PIC_CONSULTATION)) {
                    byId.setPatientOtherOrderNo(origionMessage.getStr1());
                    byId.setPatientOtherOrderStatus("0");
                    byId.setMsgId(chatMsg.getId());
                    byId.setReceiverStatus(0);
                }
                byId.setLastChatTime(new Date());
                log.info("团队聊天xiugai" + byId.toString());
                chatUserService.updateById(byId);
                //判断该患者是否在医生下面 否则添加到医生下面
                if (!fromUser.getId().equals(byId.getTargetUid())) {//代表是医生发送消息
                    saveUserFollowDoctor(fromUser, byId);
                }
                checkDoctorTeamChat(origionMessage.getChatUserId());
            } else {
                origionMessage.setMyUserId(fromUser.getId());
                Channel targetUserChannel = UserChannelManager.getUserChannel(origionMessage.getTargetUid());

                this.saveOrUpdateChatUser(fromUser, origionMessage, chatMsg);


                //3.保存或更新用户聊天
                if (chatMsg.getMsgType().equals(ChatProto.MESSAGE_PIC)) {
                    chatMsg.setMsg("[图片]");
                }
                if (chatMsg.getMsgType().equals(ChatProto.FORM)) {
                    chatMsg.setMsg("表单");
                }
                //向目标用户发送新消息提醒
                SocketFrameTextMessage targetUserMessage
                        = SocketFrameTextMessage.newMessageTip(fromUser.getId(), fromUser.getNickname(), fromUser.getAvatar(), createTime, origionMessage.getMsgType(), JSON.toJSONString(chatMsg));

                if (targetUserChannel != null) {
                    targetUserChannel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(targetUserMessage)));
                } else {
                    Integer patientId = origionMessage.getPatientId();
                    User user = userService.getById(origionMessage.getTargetUid());

                    String name = "";
                    if (StringUtils.isEmpty(user.getPatientName())) {
                        name = user.getNickname();
                    } else {
                        name = user.getPatientName();
                    }
                    if (patientId != null) {
                        PatientUser patientUser = patientUserService.getById(patientId);
                        name = patientUser.getName();
                    }
                    uniAppPushService.send("法罗适", name + ": " + origionMessage.getMsg(), origionMessage.getTargetUid() + "", "");
                    if (!StringUtils.isEmpty(user.getMpOpenId())) {

                        LocalDateTime now = LocalDateTime.now();
                        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                        String time = df.format(now);
                        wxMpService.sendDoctorTip(user.getMpOpenId(), "您有新的医生消息", name, time, origionMessage.getMsg(), "/pages/news/news");

                    }
                }

                //处理 图文咨询 发送 修改 接收状态
                updateChatReceiverStatus(origionMessage, fromUser);
            }


        } catch (Exception e) {
            log.error("消息推送失败", e);
            // 发生异常提示错误
            channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(SocketFrameTextMessage.error("消息发送失败", origionMessage))));
        }

    }

    private void sendNotic(ChatMsg chatMsg, User fromUser, SocketFrameTextMessage origionMessage, List<String> allUserIds) {
        ThreadPoolExecutorFactory.getThreadPoolExecutor().execute(new Runnable() {
            @Override
            public void run() {
                Integer patientId = origionMessage.getPatientId();
                String name = "";
                if (patientId != null) {
                    PatientUser patientUser = patientUserService.getById(patientId);
                    name = patientUser.getName();
                }
                for (String userId : allUserIds) {
                    String replace = userId.replace("[", "");
                    userId = replace.replace("]", "");
                    userId = userId.trim();
                    if (!userId.equals(fromUser.getId() + "")) {

                        Channel targetUserChannel = UserChannelManager.getUserChannel(Integer.parseInt(userId));
                        //2.向目标用户发送新消息提醒
                        SocketFrameTextMessage targetUserMessage
                                = SocketFrameTextMessage.newGroupMessageTip(origionMessage.getChatUserId(), JSON.toJSONString(chatMsg));
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
                            uniAppPushService.send("法罗适", name + ": " + origionMessage.getMsg(), userId, "");


                            if (user != null && !StringUtils.isEmpty(user.getMpOpenId())) {
                                LocalDateTime now = LocalDateTime.now();
                                DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                String time = df.format(now);

                                wxMpService.sendDoctorTip(user.getMpOpenId(), "您有新的医生消息", name, time, origionMessage.getMsg(), "/pages/news/news");
                                //wxMpService.sendDoctorUrlTip(user.getMpOpenId(), "您有新的医生消息", "", time, origionMessage.getMsg(), "https://pharos3.ewj100.com/record.html#/ucenter/recovery/externalLink");

                            }
                        }
                    }

                }
            }
        });
    }

    /**
     * 检查新加入的医生团队 ，然后加入到群聊当中
     */
    private void checkDoctorTeamChat(Integer chatUserId) {
        ThreadPoolExecutorFactory.getThreadPoolExecutor().execute(new Runnable() {
            @Override
            public void run() {
                ChatUser chatUser = chatUserService.getById(chatUserId);
                if (chatUser != null) {
                    Integer teamId = chatUser.getTeamId();
                    List<DoctorTeamPeople> list = doctorTeamPeopleService.list(new QueryWrapper<DoctorTeamPeople>().lambda()
                            .eq(DoctorTeamPeople::getTeamId, teamId));
                    if (!CollectionUtils.isEmpty(list)) {

                        List<Integer> doctorIds = list.stream().map(DoctorTeamPeople::getUserId)
                                .collect(Collectors.toList());
                        String userIds = chatUser.getUserIds();
                        List<String> split = Arrays.asList(userIds.split(","));

                        for (Integer doctorId : doctorIds) {
                            if (!split.contains(doctorId + "")) {
                                userIds = userIds + "," + doctorId;
                            }
                        }
                        chatUser.setUserIds(userIds);
                        chatUserService.updateById(chatUser);
                    }

                }
            }
        });
    }

    private void updateChatReceiverStatus(SocketFrameTextMessage origionMessage, User fromUser) {
        ThreadPoolExecutorFactory.getThreadPoolExecutor().execute(new Runnable() {
            @Override
            public void run() {
                if (origionMessage.getType() != null && origionMessage.getType().equals(1)) {
                    //代表示医生发送的消息 修改 接收回复状态 为1
                    chatUserService.update(Wrappers.<ChatUser>lambdaUpdate()
                            .eq(ChatUser::getUid, fromUser.getId())
                            .eq(ChatUser::getTargetUid, origionMessage.getTargetUid())
                            .set(ChatUser::getReceiverStatus, 1)
                    );
                }
                if (origionMessage.getMsgType().equals(ChatProto.PIC_CONSULTATION)) {
                    //代表是图文咨询修改  接收回复状态为0
                    chatUserService.update(Wrappers.<ChatUser>lambdaUpdate()
                            .eq(ChatUser::getUid, origionMessage.getTargetUid())
                            .eq(ChatUser::getTargetUid, fromUser.getId())
                            .set(ChatUser::getReceiverStatus, 0)
                    );
                }
            }
        });
    }

    private void saveUserFollowDoctor(User fromUser, ChatUser byId) {
        ThreadPoolExecutorFactory.getThreadPoolExecutor().execute(new Runnable() {
            @Override
            public void run() {
                // List<UserGroupRelationUser> userGroupRelationUserList = new ArrayList<>();

                List<UserFollowDoctor> userFollowDoctors = userFollowDoctorService.list(new QueryWrapper<UserFollowDoctor>().lambda()
                        .eq(UserFollowDoctor::getDoctorId, fromUser.getId())
                        .eq(UserFollowDoctor::getUserId, byId.getTargetUid()));
//
//                List<UserGroup> userGroupList = userGroupService.list(new QueryWrapper<UserGroup>().lambda()
//                        .isNull(UserGroup::getTeamId));
//                if (!CollectionUtils.isEmpty(userGroupList)) {
//
//                    List<Integer> userGroupIds = userGroupList.stream().map(UserGroup::getId)
//                            .collect(Collectors.toList());
//                    userGroupRelationUserList = userGroupRelationUserService.list(new QueryWrapper<UserGroupRelationUser>().lambda()
//                            .in(UserGroupRelationUser::getUserGroupId, userGroupIds)
//                            .eq(UserGroupRelationUser::getUserId, byId.getTargetUid()));
//                }
                if (CollectionUtils.isEmpty(userFollowDoctors)) {
                    //添加到医生自己的患者
                    UserFollowDoctor userFollowDoctor = new UserFollowDoctor();
                    userFollowDoctor.setDoctorId(fromUser.getId());
                    userFollowDoctor.setUserId(byId.getTargetUid());
                    userFollowDoctorService.save(userFollowDoctor);
                }

            }
        });
    }


    /**
     * 保存或更新用户聊天
     *
     * @return
     */
    private void saveOrUpdateChatUser(User fromUser, SocketFrameTextMessage origionMessage, ChatMsg chatMsg) {
        ChatUser fromUserChat = new ChatUser();
        fromUserChat.setUid(fromUser.getId());
        fromUserChat.setTargetUid(origionMessage.getTargetUid());
        fromUserChat.setLastChatTime(new Date());
        fromUserChat.setLastMsg(chatMsg.getMsg());
        if (origionMessage.getMsgType().equals(ChatProto.PIC_CONSULTATION)) {
            if (!StringUtils.isEmpty(origionMessage.getStr1())) {
                fromUserChat.setPatientOtherOrderNo(origionMessage.getStr1());

            }
            fromUserChat.setPatientOtherOrderStatus("0");
            fromUserChat.setMsgId(chatMsg.getId());
        }

        ChatUser toUserChat = new ChatUser();
        toUserChat.setUid(origionMessage.getTargetUid());
        toUserChat.setTargetUid(fromUser.getId());
        toUserChat.setLastChatTime(new Date());
        toUserChat.setLastMsg(chatMsg.getMsg());
        if (origionMessage.getMsgType().equals(ChatProto.PIC_CONSULTATION)) {
            if (!StringUtils.isEmpty(origionMessage.getStr1())) {
                toUserChat.setPatientOtherOrderNo(origionMessage.getStr1());
            }
            toUserChat.setPatientOtherOrderStatus("0");
            toUserChat.setMsgId(chatMsg.getId());
        }
        List<ChatUser> chatUsers = new ArrayList<>();
        chatUsers.add(fromUserChat);
        chatUsers.add(toUserChat);


        chatUsers.forEach(chatUser -> {
            log.info("就诊人id=========" + origionMessage.getPatientId() + "===" + origionMessage.getStr1() + "=====" + chatUser.getTargetUid() + "======" + chatUser.getUid());
            LambdaQueryWrapper<ChatUser> eq = Wrappers.<ChatUser>lambdaQuery().eq(ChatUser::getTargetUid, chatUser.getTargetUid()).eq(ChatUser::getUid, chatUser.getUid());

            if (origionMessage.getPatientId() != null) {
                eq.eq(ChatUser::getPatientId, origionMessage.getPatientId());
            } else {
                eq.isNull(ChatUser::getPatientId);
            }

            ChatUser one = chatUserService.getOne(eq);
            log.info("mmmmmm" + one.toString());
            if (one != null) {
                if (!one.getChatCount().equals(0)) {
                    one.setChatCount(one.getChatCount() - 1);
                    String patientOtherOrderNo = one.getPatientOtherOrderNo();
                    if(!StringUtils.isEmpty(patientOtherOrderNo)){
                        PatientOtherOrder patientOtherOrder=new PatientOtherOrder();
                        patientOtherOrder.setId(Integer.parseInt(patientOtherOrderNo));
                        patientOtherOrder.setNewMsg(1);
                        patientOtherOrderService.updateById(patientOtherOrder);

                    }
                }
                if (origionMessage.getType() != null && origionMessage.getType().equals(1) && chatUser.getUid().equals(fromUser.getId())) {

                    if (origionMessage.getPatientId() != null) {
                        LambdaUpdateWrapper<ChatUser> set = Wrappers.<ChatUser>lambdaUpdate()
                                .eq(ChatUser::getUid, chatUser.getUid())
                                .eq(ChatUser::getTargetUid, chatUser.getTargetUid());
                        set.eq(ChatUser::getPatientId, origionMessage.getPatientId());
                        set.set(ChatUser::getLastChatTime, chatUser.getLastChatTime());
                        set.set(ChatUser::getChatCount, one.getChatCount());
                        set.set(ChatUser::getReceiverStatus, 1);
                        if (!StringUtils.isEmpty(chatUser.getPatientOtherOrderNo())) {
                            set.set(ChatUser::getPatientOtherOrderNo, chatUser.getPatientOtherOrderNo());
                            set.set(ChatUser::getPatientOtherOrderStatus, "0");
                        }
                        chatUserService.update(set);
                    } else {
                        LambdaUpdateWrapper<ChatUser> set = Wrappers.<ChatUser>lambdaUpdate()
                                .eq(ChatUser::getUid, chatUser.getUid())
                                .isNull(ChatUser::getPatientId)
                                .eq(ChatUser::getTargetUid, chatUser.getTargetUid())
                                .set(ChatUser::getLastChatTime, chatUser.getLastChatTime())
                                .set(ChatUser::getChatCount, one.getChatCount())
                                .set(ChatUser::getReceiverStatus, 1);
                        if (!StringUtils.isEmpty(chatUser.getPatientOtherOrderNo())) {
                            set.set(ChatUser::getPatientOtherOrderNo, chatUser.getPatientOtherOrderNo());
                            set.set(ChatUser::getPatientOtherOrderStatus, "0");
                        }
                        chatUserService.update(set);
                    }
                    log.info("ddddddd" + origionMessage.getPatientId());

                } else {

                    if (origionMessage.getPatientId() != null) {
                        LambdaUpdateWrapper<ChatUser> set = Wrappers.<ChatUser>lambdaUpdate()
                                .eq(ChatUser::getUid, chatUser.getUid())
                                .eq(ChatUser::getTargetUid, chatUser.getTargetUid());
                        set.eq(ChatUser::getPatientId, origionMessage.getPatientId());
                        set.set(ChatUser::getLastChatTime, chatUser.getLastChatTime());
                        set.set(ChatUser::getChatCount, one.getChatCount());
                        set.set(ChatUser::getLastMsg, chatMsg.getMsg());
                        log.info("哇哈哈哈" + chatUser.getPatientOtherOrderStatus());
                        if (!StringUtils.isEmpty(chatUser.getPatientOtherOrderNo())) {
                            set.set(ChatUser::getPatientOtherOrderNo, chatUser.getPatientOtherOrderNo());
                            set.set(ChatUser::getPatientOtherOrderStatus, "0");
                        }
                        chatUserService.update(set);
                    } else {
                        LambdaUpdateWrapper<ChatUser> set = Wrappers.<ChatUser>lambdaUpdate()
                                .eq(ChatUser::getUid, chatUser.getUid())
                                .eq(ChatUser::getTargetUid, chatUser.getTargetUid())
                                .isNull(ChatUser::getPatientId)
                                .set(ChatUser::getLastChatTime, chatUser.getLastChatTime())
                                .set(ChatUser::getChatCount, one.getChatCount())
                                .set(ChatUser::getLastMsg, chatMsg.getMsg());
                        if (!StringUtils.isEmpty(chatUser.getPatientOtherOrderNo())) {
                            set.set(ChatUser::getPatientOtherOrderNo, chatUser.getPatientOtherOrderNo());
                            set.set(ChatUser::getPatientOtherOrderStatus, "0");
                        }
                        log.info("cccccccccccccc" + origionMessage.getPatientId());
                        chatUserService.update(set);
                    }

                }


            } else {

                chatUser.setIsClosed(0);
                if (origionMessage.getPatientId() != null) {
                    chatUser.setPatientId(origionMessage.getPatientId() + "");
                }
                chatUser.setLastChatTime(new Date());
                chatUser.setClearTime(LocalDateTime.now().minusDays(1));
                chatUser.setLastMsg(chatMsg.getMsg());
                chatUser.setRemark(origionMessage.getRemark());
                chatUserService.save(chatUser);
            }
        });
    }

    public ChatMsg saveChatMsg(SocketFrameTextMessage origionMessage, User fromUser, boolean pushed, Date date) {
        ChatMsg chatMsg = new ChatMsg();
        chatMsg.setMsgType(origionMessage.getMsgType());
        chatMsg.setFromUid(fromUser.getId());
        if (origionMessage.getPatientId() != null) {
            chatMsg.setPatientId(origionMessage.getPatientId() + "");

        }
        chatMsg.setToUid(origionMessage.getTargetUid());
        chatMsg.setMsg(origionMessage.getMsg());
        chatMsg.setCreateTime(date);
        chatMsg.setCanceled(0);
        chatMsg.setPushed(0);
        chatMsg.setReadStatus(0);
        chatMsg.setUrl(origionMessage.getUrl());
        chatMsg.setVideoDuration(origionMessage.getVideoDuration());
        chatMsg.setStr1(origionMessage.getStr1());
        chatMsg.setStr2(origionMessage.getStr2());
        chatMsg.setChatUserId(origionMessage.getChatUserId());
        chatMsg.setReadUserIds(fromUser.getId() + "");
        chatMsgService.save(chatMsg);
        return chatMsg;
    }
}

