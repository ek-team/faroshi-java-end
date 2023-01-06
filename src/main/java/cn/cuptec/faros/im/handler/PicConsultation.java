package cn.cuptec.faros.im.handler;

import cn.cuptec.faros.im.handler.base.AbstractP2PMessageHandler;
import cn.cuptec.faros.im.proto.ChatProto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 图文咨询申请
 */
@Slf4j
@Component(ChatProto.PIC_CONSULTATION)
public class PicConsultation extends AbstractP2PMessageHandler {
}
