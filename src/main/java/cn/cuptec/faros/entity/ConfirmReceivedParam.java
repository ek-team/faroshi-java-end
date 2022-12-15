package cn.cuptec.faros.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class ConfirmReceivedParam {
    private int id;
    private Integer coverStatu;
    private BigDecimal deductionAmount;
    private List<String> picUrls;

}
