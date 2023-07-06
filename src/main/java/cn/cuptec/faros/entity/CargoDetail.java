package cn.cuptec.faros.entity;

import lombok.Data;

@Data
public class CargoDetail {
    private String name;//货物名称，如果需要生成电子 运单，则为必填
    private String unit;//货物单位，如：个、台、本， 跨境件报关需要填写
    private Double weight;//订单货物单位重量，包含子母件， 单位千克，精确到小数点后3位 跨境件报关需要填写
}

