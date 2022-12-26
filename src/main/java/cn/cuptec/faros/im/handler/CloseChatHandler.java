package cn.cuptec.faros.im.handler;

import cn.cuptec.faros.entity.ChatUser;
import cn.cuptec.faros.im.bean.SocketFrameTextMessage;
import cn.cuptec.faros.im.core.SocketUser;
import cn.cuptec.faros.im.core.UserChannelManager;
import cn.cuptec.faros.im.handler.base.AbstractMessageHandler;
import cn.cuptec.faros.im.proto.ChatProto;
import cn.cuptec.faros.service.ChatUserService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.Resource;

@Component(ChatProto.REQUEST_CLOSECHAT)
public class CloseChatHandler extends AbstractMessageHandler {

    @Resource
    private ChatUserService chatUserService;

    @Override
    public void handle(Channel channel, SocketFrameTextMessage origionMsg) {

        Assert.notNull(origionMsg.getTargetUid(), "要关闭的会话不能为空");
        SocketUser userInfo = UserChannelManager.getUserInfo(channel);

        Integer uid = userInfo.getUserInfo().getId();
        Integer targetUid = origionMsg.getTargetUid();

        //更新对话状态为已关闭
        chatUserService.update(Wrappers.<ChatUser>lambdaUpdate()
                .nested(condition -> condition
                        .eq(ChatUser::getUid, uid)
                        .eq(ChatUser::getTargetUid, targetUid)
                )
                .or(condition -> condition
                        .eq(ChatUser::getUid, targetUid)
                        .eq(ChatUser::getTargetUid, uid)
                )
                .set(ChatUser::getIsClosed, true)
        );

        // 通知用户会话已关闭

        channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(SocketFrameTextMessage.closeChat(targetUid))));
        //获取目标用户channel
        Channel targetUserChannel = UserChannelManager.getUserChannel(origionMsg.getTargetUid());
        if (targetUserChannel != null){
            targetUserChannel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(SocketFrameTextMessage.closeChat(uid))));
        }

    }

}
