package cn.cuptec.faros.entity;

import cn.cuptec.faros.common.annotation.Queryable;
import cn.cuptec.faros.common.enums.QueryLogical;
import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 销售员采购单
 */
@Data
public class PurchaseOrder {

    @TableId(type = IdType.AUTO)
    private Integer id;

    @Queryable(queryLogical = QueryLogical.LIKE)
    private String orderNo;

    //采购者id
    private Integer purchaserId;

    //部门id
    @Queryable(queryLogical = QueryLogical.EQUAL)
    private Integer deptId;

    //采购说明
    private String orderNote;

    //订单状态 0-待处理 10-厂家已确认 20-已取消
    @Queryable(queryLogical = QueryLogical.EQUAL)
    private Integer status;

    //调拨状态 0-未调拨 10-部分调拨 20-全部调拨
    @Queryable(queryLogical = QueryLogical.EQUAL)
    private Integer flitStatu;

    //创建时间
    private Date createTime;

    //厂家确认采购时间
    private Date confirmTime;

    //乐观锁
    @Version
    @JSONField(deserialize = false, serialize = false)
    private Integer version;

    @TableField(exist = false)
    private String deptName;

    @TableField(exist = false)
    private String salesmanName;

    @TableField(exist = false)
    private List<PurchaseOrderItem> purchaseOrderItemList;


    // 是否有确认权限 0.无权限 1.有权限
    @TableField(exist = false)
    private Integer isConfirm;

}
