package cn.cuptec.faros.im.handler;

import cn.cuptec.faros.im.handler.base.AbstractP2PMessageHandler;
import cn.cuptec.faros.im.proto.ChatProto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 咨询消息医生确认消息
 * str1//0-待接收 1-接收 2-拒绝
 * str2//订单id
 */
@Slf4j
@Component(ChatProto.CONFIRM_STATUS)
public class ConfirmStatusHandler extends AbstractP2PMessageHandler {
}
