package cn.cuptec.faros.im.handler;

import cn.cuptec.faros.im.handler.base.AbstractP2PMessageHandler;
import cn.cuptec.faros.im.proto.ChatProto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 表单消息
 */
@Slf4j
@Component(ChatProto.FORM)
public class FormHandler extends AbstractP2PMessageHandler {
}
