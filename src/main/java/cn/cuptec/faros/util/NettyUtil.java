package cn.cuptec.faros.util;

import cn.cuptec.faros.im.proto.ChatProto;
import com.alibaba.fastjson.JSON;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.net.SocketAddress;

public class NettyUtil {

    /**
     * 获取Channel的远程IP地址
     * @param channel
     * @return
     */
    public static String parseChannelRemoteAddr(final Channel channel) {
        if (null == channel) {
            return "";
        }
        SocketAddress remote = channel.remoteAddress();
        final String addr = remote != null ? remote.toString() : "";

        if (addr.length() > 0) {
            int index = addr.lastIndexOf("/");
            if (index >= 0) {
                return addr.substring(index + 1);
            }

            return addr;
        }

        return "";
    }

    /**
     * 发送文本消息
     * @param channel
     * @param chatProto
     */
    public static void writeTextWebSocketFrame(Channel channel, ChatProto chatProto){
        channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(chatProto)));
    }

}
