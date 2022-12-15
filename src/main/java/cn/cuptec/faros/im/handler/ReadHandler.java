package cn.cuptec.faros.im.handler;

import cn.cuptec.faros.im.bean.SocketFrameTextMessage;
import cn.cuptec.faros.im.core.SocketUser;
import cn.cuptec.faros.im.core.UserChannelManager;
import cn.cuptec.faros.im.handler.base.AbstractMessageHandler;
import cn.cuptec.faros.im.proto.ChatProto;
import cn.cuptec.faros.service.ChatMsgService;
import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.util.Date;

/**
 * 设置消息已读
 */
@Slf4j
@Component(ChatProto.REQUEST_READ)
public class ReadHandler extends AbstractMessageHandler {

    @Resource
    private ChatMsgService chatMsgService;

    @Override
    public void handle(Channel channel, SocketFrameTextMessage origionMsg) {
        Assert.notNull(origionMsg.getTargetUid(), "目标用户不能为空");

        SocketUser userInfo = UserChannelManager.getUserInfo(channel);

        Long timestamp = origionMsg.getMsgTimeStamp();

        Date date = new Date(timestamp);

        TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame(JSON.toJSONString(SocketFrameTextMessage.responseSetReaded(timestamp)));
        channel.writeAndFlush(textWebSocketFrame);

        Channel userChannel = UserChannelManager.getUserChannel(origionMsg.getTargetUid());
        if (userChannel != null) {
            TextWebSocketFrame textWebSocketFrame2 = new TextWebSocketFrame(
                    JSON.toJSONString(SocketFrameTextMessage.responseSetReaded(timestamp))
            );
            userChannel.writeAndFlush(textWebSocketFrame2);
        }

        chatMsgService.setReaded(userInfo.getUserInfo().getId(), origionMsg.getTargetUid(), date);

    }

}
