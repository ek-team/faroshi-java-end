package cn.cuptec.faros.im.handler;

import cn.cuptec.faros.im.bean.SocketFrameTextMessage;
import cn.cuptec.faros.im.core.UserChannelManager;
import cn.cuptec.faros.im.handler.base.AbstractMessageHandler;
import cn.cuptec.faros.im.proto.ChatProto;
import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component(ChatProto.REQUEST_AUTH)
public class AuthHandler extends AbstractMessageHandler {


    @Override
    public void handle(Channel channel, SocketFrameTextMessage origionMsg) {
        log.info("发送认证请求:{}",origionMsg.getUserInfo());
        boolean authed = UserChannelManager.saveUser(channel, origionMsg);
        channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(SocketFrameTextMessage.authResult(authed))));
        if (authed){
            //登录成功

        }
    }

}
