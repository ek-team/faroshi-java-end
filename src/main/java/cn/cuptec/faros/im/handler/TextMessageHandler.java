package cn.cuptec.faros.im.handler;

import cn.cuptec.faros.im.handler.base.AbstractP2PMessageHandler;
import cn.cuptec.faros.im.proto.ChatProto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 用户文本消息处理
 */
@Slf4j
@Component(ChatProto.MESSAGE_TEXT)
public class TextMessageHandler extends AbstractP2PMessageHandler {


}
