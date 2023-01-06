package cn.cuptec.faros.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class CalculatePriceResult {
    private Double rent;//每天租金
    private Double deposit;//押金
    private Double recoveryPrice;//回收价
    private BigDecimal totalAmount;//总价
}
