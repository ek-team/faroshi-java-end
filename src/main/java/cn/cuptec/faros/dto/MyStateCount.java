package cn.cuptec.faros.dto;

import lombok.Data;

@Data
public class MyStateCount {
    private int pendingPayment;//待付款
    private int pendingDelivery;//待发货
    private int pendingReward;//待收货
}
