package cn.cuptec.faros.entity;

import lombok.Data;

@Data
public class ApiResultData {
    private Boolean success;
    private String errorCode;
    private String errorMsg;
    private MsgData  msgData;
    private MianDanObj obj;
}
