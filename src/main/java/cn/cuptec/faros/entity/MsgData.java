package cn.cuptec.faros.entity;

import lombok.Data;

import java.util.List;

@Data
public class MsgData {
    private String orderId;

    private List<WaybillNoInfo> waybillNoInfoList;//顺丰运单号

    private List<RouteLabelInfo> routeLabelInfo;//路由标签，除少量特殊场景用户外，其余均会返回
}
