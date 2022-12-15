package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

/**
 * 退款信息
 */
@Data
@TableName(value = "order_refund_info")
public class OrderRefundInfo implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    @TableField(value = "order_no")
    private String orderNo;
    private String orderRefundNo;

    @TableField(value = "order_id")
    private String orderId;

    @TableField(value = "mch_id")
    private String mchId;

    @TableField(value = "transaction_id")
    private String transactionId;

    @TableField(value = "refund_fee")
    private BigDecimal refundFee;

    @TableField(value = "retrieve_order_id")
    private Integer retrieveOrderId;

    @TableField(value = "success_time")
    private Date successTime;

    //退款状态  1.等待通知   2.退款成功 3.退款异常 4.退款关闭
    @TableField(value = "refund_status")
    private Integer refundStatus;


    private Integer orderPayId;

    private BigDecimal totalFee;

    private static final long serialVersionUID = 1L;
}