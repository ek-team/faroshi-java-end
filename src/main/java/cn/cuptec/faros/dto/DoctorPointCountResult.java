package cn.cuptec.faros.dto;

import lombok.Data;

@Data
public class DoctorPointCountResult {
    private Integer totalPoint;//总积分
    private Integer pendingWithdraw;//待提现
    private Integer withdraw;//已提现
}
