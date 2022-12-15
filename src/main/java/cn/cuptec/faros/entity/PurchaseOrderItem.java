package cn.cuptec.faros.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.Data;

@Data
public class PurchaseOrderItem {

    private Integer id;

    private Integer purchaseOrderId;

    private Integer productId;

    //采购数量
    private Integer purchaseCount;

    //锁定数量
    private Integer lockCount;

    //已发货数量
    private Integer deliveryCount;

    @Version
    @JSONField(deserialize = false, serialize = false)
    private Integer version;

    //删除标记
    @TableLogic
    @JSONField(deserialize = false, serialize = false)
    private String delFlag;

    @TableField(exist = false)
    private String productName;

}
