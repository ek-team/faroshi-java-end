package cn.cuptec.faros.pay;

import lombok.Data;

@Data
public class PayResultData {
    private String appId;
    private String timeStamp;
    private String nonceStr;
    private String packageValue;
    private String signType;
    private String paySign;
    private Integer orderId;
}
