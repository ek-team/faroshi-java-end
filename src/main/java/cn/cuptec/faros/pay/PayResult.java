package cn.cuptec.faros.pay;

import lombok.Data;

@Data
public class PayResult {
    private Integer code;
    private String msg;
    private PayResultData data;
}
