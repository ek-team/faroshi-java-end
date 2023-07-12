package cn.cuptec.faros.entity;

import lombok.Data;

import java.util.Date;
import java.util.List;

@Data
public class SFMsgData {
    private String language = "zh-CN";
    private String orderId;
    private Date sendStartTm;//要求上门取件开始时间
    private List<CargoDetail> cargoDetails;//托寄物信息
    private String cargoDesc;//	拖寄物类型描述,如： 文件，电子产品，衣服等
    private List<ContactInfo> contactInfoList;//收寄双方信息
    private String monthlyCard="5560332054";//顺丰月结卡号
    private Integer expressTypeId = 1;
    private Integer isReturnRoutelabel = 1;
    private Integer isDocall = 1;

}
