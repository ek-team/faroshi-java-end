package cn.cuptec.faros.dto;

import lombok.Data;

@Data
public class KuaiDiCallBackData {
    private String courierName;
    private String defPrice;
    private String mktId;
    private String orderId;
    private String courierMobile;
    private String price;
    private String status;
    private String freight;
    private String weight;
    private String label;//面单链接
}
