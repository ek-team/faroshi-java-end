package cn.cuptec.faros.im.handler;

import cn.cuptec.faros.entity.ChatMsg;
import cn.cuptec.faros.im.bean.SocketFrameTextMessage;
import cn.cuptec.faros.im.core.UserChannelManager;
import cn.cuptec.faros.im.handler.base.AbstractMessageHandler;
import cn.cuptec.faros.im.proto.ChatProto;
import cn.cuptec.faros.service.ChatMsgService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.Resource;

/**
 * 设置音频消息已读
 */
@Slf4j
@Component(ChatProto.REQUEST_VIDEO_READ)
public class VideoReadHandler extends AbstractMessageHandler {


    @Resource
    private ChatMsgService chatMsgService;

    @Override
    public void handle(Channel channel, SocketFrameTextMessage origionMsg) {
        Assert.notNull(origionMsg.getTargetUid(), "目标用户不能为空");
        Assert.notNull(origionMsg.getMsgId(), "消息id不能为空");

        Channel userChannel = UserChannelManager.getUserChannel(origionMsg.getTargetUid());
        if (userChannel != null) {
            TextWebSocketFrame textWebSocketFrame2 = new TextWebSocketFrame(
                    JSON.toJSONString(SocketFrameTextMessage.responseSetVideoReaded(origionMsg.getMsgId()))
            );
            userChannel.writeAndFlush(textWebSocketFrame2);
        }

        chatMsgService.update(Wrappers.<ChatMsg>lambdaUpdate()
                .eq(ChatMsg::getId, origionMsg.getMsgId())
                .set(ChatMsg::getVideoRead, 2));

    }

}
