package cn.cuptec.faros.im.listener;

import cn.cuptec.faros.im.event.ChatMsgPushEvent;
import cn.cuptec.faros.service.ChatMsgService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ChatMsgPushListener implements ApplicationListener<ChatMsgPushEvent> {

    @Autowired
    private ChatMsgService chatMsgService;

    @Override
    public void onApplicationEvent(ChatMsgPushEvent chatMsgPushEvent) {
//        List<ChatMsg> msgs = chatMsgPushEvent.getMsgs();
//        //将状态为未push得
//        List<ChatMsg> unpushedMsgs = msgs.stream().filter(chatMsg -> chatMsg.getPushed() == false && chatMsg.getToUid().equals(chatMsgPushEvent.getTargetUid())).collect(Collectors.toList());
//        List<String> ids = unpushedMsgs.stream().map(chatMsg -> chatMsg.getId()).collect(Collectors.toList());
//        if (ids.size() > 0){
//            chatMsgService.setPushed(ids);
//        }
    }

}

