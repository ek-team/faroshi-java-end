package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import lombok.Data;

@Data
@TableName(value = "order_pay_info")
public class OrderPayInfo implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    @TableField(value = "order_no")
    private String orderNo;

    @TableField(value = "order_id")
    private String orderId;

    @TableField(value = "mch_id")
    private String mchId;

    @TableField(value = "transaction_id")
    private String transactionId;

    @TableField(value = "total_fee")
    private BigDecimal totalFee;

    @TableField(value = "pay_finish_time_end")
    private String payFinishTimeEnd;

    private String mchKey;

    private static final long serialVersionUID = 1L;


}