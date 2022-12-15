package cn.cuptec.faros.common.enums;


import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ImMsgCallbackCommandEnum {
    C2C_CALLBACK_AFTER_SEND_MSG("C2C.CallbackAfterSendMsg","发单聊消息之后回调"),
    C2C_CALLBACK_BEFORE_SEND_MSG("C2C.CallbackBeforeSendMsg","发单聊消息之前回调");




    private String callbackCommand;
    private String Str;



    public static ImMsgCallbackCommandEnum getEnumByCallbackCommand(String callbackCommand){

        for (ImMsgCallbackCommandEnum e:ImMsgCallbackCommandEnum.values()  ) {
            if(e.getCallbackCommand().equals(callbackCommand)) return e;
        }
        return null;
    }
}
