package cn.cuptec.faros.vo;

import lombok.Data;

/**
 * 产品虚拟库存信息
 */
@Data
public class ProductFlittingStockInfoVo {

    private int productId;

    //总数量
    private int totalCount;

    //已发货数量
    private int deliveryCount;

    //锁定数量
    private int lockCount;

    private String productName;

}
