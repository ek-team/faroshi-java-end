package cn.cuptec.faros.im.handler;

import cn.cuptec.faros.im.bean.SocketFrameTextMessage;
import cn.cuptec.faros.im.handler.base.AbstractMessageHandler;
import cn.cuptec.faros.im.proto.ChatProto;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component(ChatProto.BASE_PING)
public class PingHandler extends AbstractMessageHandler {
    @Override
    public void handle(Channel channel, SocketFrameTextMessage origionMessage) {
        log.info("receive ping message");
        return;
    }
}

