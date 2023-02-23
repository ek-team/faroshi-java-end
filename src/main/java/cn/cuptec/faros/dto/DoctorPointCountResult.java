package cn.cuptec.faros.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class DoctorPointCountResult {
    private BigDecimal totalPoint;//总积分
    private BigDecimal pendingWithdraw;//待提现
    private BigDecimal withdraw;//已提现
}
