package cn.cuptec.faros.im.handler;

import cn.cuptec.faros.im.handler.base.AbstractP2PMessageHandler;
import cn.cuptec.faros.im.proto.ChatProto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 图片消息
 */
@Slf4j
@Component(ChatProto.MESSAGE_PIC)
public class PicHandler extends AbstractP2PMessageHandler {


}
