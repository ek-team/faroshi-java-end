package cn.cuptec.faros.vo;

import cn.cuptec.faros.entity.Product;
import lombok.Data;

@Data
public class ProductVo extends Product {

    //产品序列号
    private String productSn;

    //产品唯一码
    private String liveQrCodeId;

    private String productId;
}
