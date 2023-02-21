package cn.cuptec.faros.im.handler;

import cn.cuptec.faros.im.handler.base.AbstractP2PMessageHandler;
import cn.cuptec.faros.im.proto.ChatProto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 视频消息
 */
@Slf4j
@Component(ChatProto.VIDEO_URL)
public class VideoUrlHandler extends AbstractP2PMessageHandler {
}
