package cn.cuptec.faros.entity;

import cn.cuptec.faros.common.annotation.Queryable;
import cn.cuptec.faros.common.enums.QueryLogical;
import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * 调拨单
 * 代理商在厂家拥有一定库存，调拨单是用户将厂家库存发送到代理商手里的操作
 */
@Data
public class FlittingOrder{

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    //调拨单号
    @Queryable(queryLogical = QueryLogical.LIKE)
    private String orderNo;

    //调拨仓库id  调拨到哪里
    private Integer locatorId;

    //0-待发货 1.部分发货 10-待收货 20-已收货  30-拒绝
    @Queryable(queryLogical = QueryLogical.EQUAL)
    private Integer status;

    //调拨备注
    private String orderNote;

    //创建人
    private Integer createBy;

    private Integer deptId;

    private Date createTime;

    //乐观锁
    @Version
    @JSONField(deserialize = false, serialize = false)
    private Integer version;

    @TableField(exist = false)
    private String deptName;

    @TableField(exist = false)
    private String locatorName;

    //申请人姓名
    @TableField(exist = false)
    private String createrName;

    @TableField(exist = false)
    private List<FlittingOrderItem> flittingOrderItems;

}
