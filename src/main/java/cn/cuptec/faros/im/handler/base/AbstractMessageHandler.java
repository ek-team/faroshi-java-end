package cn.cuptec.faros.im.handler.base;

import cn.cuptec.faros.im.bean.SocketFrameTextMessage;
import cn.cuptec.faros.im.core.UserChannelManager;
import io.netty.channel.Channel;

public abstract class AbstractMessageHandler implements MessageHandler {

    @Override
    public void begin(Channel channel, SocketFrameTextMessage origionMsg) {
        pre(channel, origionMsg);
        handle(channel, origionMsg);
        after(channel, origionMsg);
    }

    public abstract void handle(Channel channel, SocketFrameTextMessage origionMsg);

    public void pre(Channel channel, SocketFrameTextMessage origionMsg) {
        UserChannelManager.updateUserTime(channel);
    }

    public void after(Channel channel, SocketFrameTextMessage origionMsg) {

    }

}
