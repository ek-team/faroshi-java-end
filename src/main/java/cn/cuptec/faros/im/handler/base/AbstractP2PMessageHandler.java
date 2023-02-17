package cn.cuptec.faros.im.handler.base;

import cn.cuptec.faros.entity.ChatMsg;
import cn.cuptec.faros.entity.ChatUser;
import cn.cuptec.faros.entity.User;
import cn.cuptec.faros.im.bean.SocketFrameTextMessage;
import cn.cuptec.faros.im.core.SocketUser;
import cn.cuptec.faros.im.core.UserChannelManager;
import cn.cuptec.faros.im.proto.ChatProto;
import cn.cuptec.faros.service.ChatMsgService;
import cn.cuptec.faros.service.ChatUserService;
import cn.cuptec.faros.service.UniAppPushService;
import cn.cuptec.faros.service.UserService;
import com.alibaba.fastjson.JSON;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

@Slf4j
public abstract class AbstractP2PMessageHandler extends AbstractMessageHandler {

    @Resource
    private ChatMsgService chatMsgService;
    @Resource
    private ChatUserService chatUserService;
    @Resource
    private UniAppPushService uniAppPushService;
    @Resource
    private cn.cuptec.faros.service.WxMpService wxMpService;
    @Resource
    private UserService userService;

    @Override
    @Transactional
    public void handle(Channel channel, SocketFrameTextMessage origionMessage) {

        try {
            Date createTime = new Date();
            //
            SocketUser userInfo = UserChannelManager.getUserInfo(channel);
            User fromUser = userInfo.getUserInfo();
            if (fromUser.getId() == null) {
                channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(SocketFrameTextMessage.authRequired())));
                return;
            }
            //保存新消息到记录中
            if (origionMessage.getMsgType().equals(ChatProto.PIC_CONSULTATION)) {
                origionMessage.setMsg("图文咨询");
                origionMessage.setStr2("0");//0-待接收 1-接收 2-拒绝
            }

            ChatMsg chatMsg = saveChatMsg(origionMessage, fromUser, false, new Date());
            chatMsg.setMsg(origionMessage.getMsg());
            log.info("收到消息=======================" + origionMessage.toString());
            channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(SocketFrameTextMessage.responseMessage(chatMsg))));
            User byId1 = userService.getById(chatMsg.getFromUid());
            chatMsg.setUser(byId1);
            //判断是否是群聊
            if (origionMessage.getChatUserId() != null) {
                //群发消息
                ChatUser byId = chatUserService.getById(origionMessage.getChatUserId());
                //推送群聊的消息给所有人
                String data = byId.getUserIds();
                List<String> allUserIds = Arrays.asList(data.split(","));
                for (String userId : allUserIds) {
                    if (!userId.equals(fromUser.getId() + "")) {
                        Channel targetUserChannel = UserChannelManager.getUserChannel(Integer.parseInt(userId));
                        //2.向目标用户发送新消息提醒
                        SocketFrameTextMessage targetUserMessage
                                = SocketFrameTextMessage.newGroupMessageTip(origionMessage.getChatUserId(), JSON.toJSONString(chatMsg));
                        if (targetUserChannel != null) {
                            targetUserChannel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(targetUserMessage)));
                        } else {
                            uniAppPushService.send("法罗适", origionMessage.getMsg(), userId, "");
                            User user = userService.getById(userId);
                            if (!StringUtils.isEmpty(user.getMpOpenId())) {
                                LocalDateTime now = LocalDateTime.now();
                                DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                                String time = df.format(now);
                                wxMpService.sendDoctorTip(user.getMpOpenId(), "您有新的医生消息", "", time, origionMessage.getMsg(), "/pages/news/news");

                            }
                        }
                    }

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
                }
                byId.setLastChatTime(new Date());
                chatUserService.updateById(byId);
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
                    uniAppPushService.send("法罗适", origionMessage.getMsg(), origionMessage.getTargetUid() + "", "");

                }
            }


        } catch (Exception e) {
            log.error("消息推送失败", e);
            // 发生异常提示错误
            channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(SocketFrameTextMessage.error("消息发送失败", origionMessage))));
        }

    }

    public static void main(String[] args) {
        String data = "你好中国";
//        List<Term> segment = HanLP.segment(data);
//        for(Term term:segment){
//            System.out.println(term.word);
//        }
        System.out.println(data.indexOf("你好中国"));
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
            fromUserChat.setPatientOtherOrderNo(origionMessage.getStr1());
            fromUserChat.setPatientOtherOrderStatus("0");
        }

        ChatUser toUserChat = new ChatUser();
        toUserChat.setUid(origionMessage.getTargetUid());
        toUserChat.setTargetUid(fromUser.getId());
        toUserChat.setLastChatTime(new Date());
        toUserChat.setLastMsg(chatMsg.getMsg());
        if (origionMessage.getMsgType().equals(ChatProto.PIC_CONSULTATION)) {
            toUserChat.setPatientOtherOrderNo(origionMessage.getStr1());
            toUserChat.setPatientOtherOrderStatus("0");
        }
        List<ChatUser> chatUsers = new ArrayList<>();
        chatUsers.add(fromUserChat);
        chatUsers.add(toUserChat);

        chatUsers.forEach(chatUser -> {
            ChatUser one = chatUserService.getOne(Wrappers.<ChatUser>lambdaQuery().eq(ChatUser::getTargetUid, chatUser.getTargetUid()).eq(ChatUser::getUid, chatUser.getUid()));
            if (one != null) {

                chatUserService.update(Wrappers.<ChatUser>lambdaUpdate()
                        .eq(ChatUser::getUid, chatUser.getUid())
                        .eq(ChatUser::getTargetUid, chatUser.getTargetUid())
                        .set(ChatUser::getLastChatTime, chatUser.getLastChatTime())
                        .set(ChatUser::getLastMsg, chatMsg.getMsg()));


            } else {

                chatUser.setIsClosed(0);
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

