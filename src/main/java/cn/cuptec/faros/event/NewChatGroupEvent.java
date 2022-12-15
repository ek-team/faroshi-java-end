package cn.cuptec.faros.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NewChatGroupEvent {

    private Integer groupId;

}
