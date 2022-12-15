package cn.cuptec.faros.im.event;

import cn.cuptec.faros.entity.ChatMsg;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

import java.util.List;

@Getter
@Setter
public class ChatMsgPushEvent extends ApplicationEvent {

    private String targetUid;

    private List<ChatMsg> msgs;

    public ChatMsgPushEvent(Object source) {
        super(source);
    }

}