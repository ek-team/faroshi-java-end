package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import net.sf.cglib.core.Local;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 续租订单
 */
@Data
public class RentRuleOrder extends Model<RentRuleOrder> {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String transactionId;
    private String userOrderNo;//订单号

    private String rentRuleOrderNo;//续租订单号

    private String day;//续租天数
    private Integer serviceCount;//图文咨询次数
    private BigDecimal amount;//金额
    private Integer userId;
    private Integer status;//1-代付款 2-已付款
    private LocalDateTime createTime;
}
