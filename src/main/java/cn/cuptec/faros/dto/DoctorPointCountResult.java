package cn.cuptec.faros.dto;

import lombok.Data;

@Data
public class DoctorPointCountResult {
    private Double totalPoint;//总积分
    private Double pendingWithdraw;//待提现
    private Double withdraw;//已提现
}
