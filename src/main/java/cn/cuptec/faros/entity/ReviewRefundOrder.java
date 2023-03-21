package cn.cuptec.faros.entity;

import cn.cuptec.faros.common.annotation.Queryable;
import cn.cuptec.faros.common.enums.QueryLogical;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 审核 退款订单
 */
@Data
public class ReviewRefundOrder {
    @TableId
    private Integer id;
    @Queryable(queryLogical = QueryLogical.LIKE)
    private String  retrieveOrderNo;//回收单单号
    @Queryable(queryLogical = QueryLogical.EQUAL)
    private Integer status;//3=待审核  2 -已拒绝 1-退款
    private String reviewRefundDesc;//审核描述
    private String refundReason;//退款原因
    private BigDecimal refundFee;//退款金额
    private Integer deptId;
    private LocalDateTime createTime;
}
