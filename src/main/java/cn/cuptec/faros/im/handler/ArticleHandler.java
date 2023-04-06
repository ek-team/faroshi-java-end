package cn.cuptec.faros.im.handler;

import cn.cuptec.faros.im.handler.base.AbstractP2PMessageHandler;
import cn.cuptec.faros.im.proto.ChatProto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 文章消息
 */
@Slf4j
@Component(ChatProto.ARTICLE)
public class ArticleHandler extends AbstractP2PMessageHandler {
}
