package cn.cuptec.faros.entity;

import lombok.Data;

import java.util.List;

/**
 * 顺丰下单返回结果
 */
@Data
public class SFMsgDataResult {
    private String apiErrorMsg;
    private String apiResponseID;
    private String apiResultCode;
    private ApiResultData apiResultData;

}
