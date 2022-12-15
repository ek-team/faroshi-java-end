package cn.cuptec.faros.vo;

import cn.cuptec.faros.entity.Product;
import lombok.Data;

@Data
public class ProductStockInfoVo extends Product {

    private int totalCount;

    //业务库存数量
    private int salesmanInStockCount;

    //销售锁定数量
    private int salesmanSaleLockCount;

    //销售中的数量
    private int salesmanSaledCount;

    //回收中的数量
    private int salesmanRetrievingCount;

}
