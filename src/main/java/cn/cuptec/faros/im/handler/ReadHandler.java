package cn.cuptec.faros.im.handler;

import cn.cuptec.faros.entity.ChatMsg;
import cn.cuptec.faros.im.bean.SocketFrameTextMessage;
import cn.cuptec.faros.im.core.SocketUser;
import cn.cuptec.faros.im.core.UserChannelManager;
import cn.cuptec.faros.im.handler.base.AbstractMessageHandler;
import cn.cuptec.faros.im.proto.ChatProto;
import cn.cuptec.faros.service.ChatMsgService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 设置消息已读
 */
@Slf4j
@Component(ChatProto.REQUEST_READ)
public class ReadHandler extends AbstractMessageHandler {

    @Resource
    private ChatMsgService chatMsgService;

    @Override
    public void handle(Channel channel, SocketFrameTextMessage origionMsg) {

        SocketUser userInfo = UserChannelManager.getUserInfo(channel);

        Integer chatUserId = origionMsg.getChatUserId();//有群聊id
        if (chatUserId == null) {

            chatMsgService.setReaded(userInfo.getUserInfo().getId(), origionMsg.getTargetUid());

        } else {
            List<ChatMsg> chatMsgs = chatMsgService.list(new QueryWrapper<ChatMsg>().lambda()
                    .eq(ChatMsg::getChatUserId, chatUserId)
            .notLike(ChatMsg::getReadUserIds,userInfo.getUserInfo().getId()));
            if (!CollectionUtils.isEmpty(chatMsgs)) {
                List<ChatMsg> updateChatMsg = new ArrayList<>();
                for (ChatMsg chatMsg : chatMsgs) {
                    String readUserIds = chatMsg.getReadUserIds();
                    if (StringUtils.isEmpty(readUserIds)) {
                        readUserIds = userInfo.getUserInfo().getId() + "";
                        chatMsg.setReadUserIds(readUserIds);
                        updateChatMsg.add(chatMsg);
                    } else {
                        if (readUserIds.indexOf(userInfo.getUserInfo().getId() + "") < 0) {
                            readUserIds = readUserIds + "," + userInfo.getUserInfo().getId();
                            chatMsg.setReadUserIds(readUserIds);
                            updateChatMsg.add(chatMsg);
                        }
                    }

                }
                if (!CollectionUtils.isEmpty(updateChatMsg)) {
                    chatMsgService.updateBatchById(updateChatMsg);
                }
            }
        }

    }

}
