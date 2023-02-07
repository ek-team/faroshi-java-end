package cn.cuptec.faros.im.handler;

import cn.cuptec.faros.im.bean.SocketFrameTextMessage;
import cn.cuptec.faros.im.core.UserChannelManager;
import cn.cuptec.faros.im.handler.base.AbstractMessageHandler;
import cn.cuptec.faros.im.proto.ChatProto;
import cn.cuptec.faros.service.ChatUserService;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
@Slf4j
@Component(ChatProto.REMOVE_CHANNEL)
public class CloseChatHandler extends AbstractMessageHandler {

    @Resource
    private ChatUserService chatUserService;

    @Override
    public void handle(Channel channel, SocketFrameTextMessage origionMsg) {
        log.info("发送退出消息发送退出消息发送退出消息发送退出消息发送退出消息");
        UserChannelManager.removeChannel(channel);
    }

}
