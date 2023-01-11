package cn.cuptec.faros.dto;

import lombok.Data;

@Data
public class KuaiDiXiaDanParam {
    public String com;
    public String recManName;
    private String recManMobile;
    private String recManPrintAddr;
    private String sendManName;
    private String sendManMobile;
    private String sendManPrintAddr;
    private String cargo;
    private String weight;
    private String remark;
    private String pickupStartTime;
    private String pickupEndTime;
    private String dayType;
    private String orderNo;//短的订单id

}
