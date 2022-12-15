package cn.cuptec.faros.im.handler.base;

import cn.cuptec.faros.im.bean.SocketFrameTextMessage;
import io.netty.channel.Channel;

public interface MessageHandler {

    void begin(Channel channel, SocketFrameTextMessage socketFrameTextMessage);

}