package cn.cuptec.faros.im.core;

import cn.cuptec.faros.common.utils.SpringUtils;
import cn.cuptec.faros.config.properties.WsProperties;
import cn.cuptec.faros.im.bean.SocketFrameTextMessage;
import cn.cuptec.faros.im.handler.base.MessageHandler;
import cn.cuptec.faros.im.proto.ChatProto;
import cn.cuptec.faros.util.NettyUtil;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;

@Slf4j
public class HandlerRouter extends ChannelInboundHandlerAdapter {

    private WebSocketServerHandshaker handshaker;

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        try{
            if (msg instanceof FullHttpRequest) {
                handleHttpRequest(ctx, (FullHttpRequest) msg);
            } else if (msg instanceof WebSocketFrame) {
                handleWebSocket(ctx, (WebSocketFrame) msg);
            }
        }catch (Exception e){
            throw e;
        }finally{
            ReferenceCountUtil.release(msg);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent evnet = (IdleStateEvent) evt;
            // 判断Channel是否读空闲, 读空闲时移除Channel
            if (evnet.state().equals(IdleState.READER_IDLE)) {
                final String remoteAddress = NettyUtil.parseChannelRemoteAddr(ctx.channel());
                log.warn("判断Channel是否读空闲, 读空闲时移除Channel");
                //UserChannelManager.removeChannel(ctx.channel());

                //todo 广播用户数量改变
            }
        }
        ctx.fireUserEventTriggered(evt);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        log.info("todo 广播用户数量改变");
        UserChannelManager.removeChannel(ctx.channel());
        //todo 广播用户数量改变
        super.channelUnregistered(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.info("todo 广播用户数量改变=====");
        UserChannelManager.removeChannel(ctx.channel());
        //todo 广播用户数量改变
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest request) {
        if (!request.decoderResult().isSuccess() || !"websocket".equals(request.headers().get("Upgrade"))) {
            log.warn("protobuf don't support websocket");
            ctx.channel().close();
            return;
        }
        WebSocketServerHandshakerFactory handshakerFactory = new WebSocketServerHandshakerFactory(
                SpringUtils.getBean(WsProperties.class).getWebSoctetUrl(), null, true);
        handshaker = handshakerFactory.newHandshaker(request);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            // 动态加入websocket的编解码处理
            handshaker.handshake(ctx.channel(), request);
            SocketUser userInfo = new SocketUser();
            userInfo.setAddr(NettyUtil.parseChannelRemoteAddr(ctx.channel()));
            // 存储已经连接的Channel
            UserChannelManager.addChannel(ctx.channel());
        }
    }

    private void handleWebSocket(ChannelHandlerContext ctx, WebSocketFrame frame) {
        // 判断是否关闭链路命令
        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            //log.info("判断是否关闭链路命令");
            UserChannelManager.removeChannel(ctx.channel());
            return;
        }
        // 判断是否Ping消息
        if (frame instanceof PingWebSocketFrame) {
            log.info("ping message:{}", frame.content().retain());
            ctx.writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
            return;
        }
        // 判断是否Pong消息
        if (frame instanceof PongWebSocketFrame) {
            log.info("pong message:{}", frame.content().retain());
            ctx.writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
            return;
        }

        Channel channel = ctx.channel();

        // 目前只需要支持文本消息
        if (!(frame instanceof TextWebSocketFrame)) {
            //throw new UnsupportedOperationException(frame.getClass().getName() + " frame type not supported");
            log.error("暂不持支此消息类型: {}", frame.getClass().getName());
            channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(SocketFrameTextMessage.error("暂不支持此消息类型"))));
            return;
        }

        String message = ((TextWebSocketFrame) frame).text();

        //log.info("[收到SOCKET消息:{}]", message);

        SocketFrameTextMessage socketFrameTextMessage = JSON.parseObject(message, SocketFrameTextMessage.class);
        try{
            //获取消息类型
            String msgType = socketFrameTextMessage.getMsgType();

            if(StringUtils.isNotEmpty(msgType)){

                SocketUser userInfo = UserChannelManager.getUserInfo(channel);
                if(userInfo==null){
                    channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(SocketFrameTextMessage.authRequired())));
                    return;
                }
                //用户未认证并且该请求非认证请求，直接返回需要认证
                if (!userInfo.isAuth() && !msgType.equals(ChatProto.REQUEST_AUTH)){
                    channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(SocketFrameTextMessage.authRequired())));
                    return;
                }
                //否则交给对应的handler处理
                else {
                    MessageHandler messageHandler = SpringUtils.<MessageHandler>getBean(msgType);
                    if (messageHandler != null){
                        messageHandler.begin(channel, socketFrameTextMessage);
                        return ;
                    }
                }
            }
            channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(SocketFrameTextMessage.error("暂不支持此消息类型或系统故障"))));

            //执行后续Handler  暂不需要执行后续handler
            //ctx.fireChannelRead(frame.retain());
        }
        catch (Exception e){
            log.error("[SOCKET消息处理失败, 消息内容：{}]", message, e);
            channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString(SocketFrameTextMessage.error(e.getMessage()))));
        }
    }

    private String getMessage(ByteBuf buf) {
        byte[] con = new byte[buf.readableBytes()];
        buf.readBytes(con);
        try {
            return new String(con, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return null;
        }
    }


}
