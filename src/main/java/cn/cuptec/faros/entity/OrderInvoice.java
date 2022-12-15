package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

@Data
@TableName(value = "order_invoice")
public class OrderInvoice implements Serializable {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 创建时间
     */
    @TableField(value = "create_time")
    private Date createTime;

    /**
     * 修改时间
     */
    @TableField(value = "update_time")
    private Date updateTime;

    /**
     * 发票类型
     */
    @TableField(value = "type")
    private Integer type;

    /**
     * 发票内容类型
     */
    @TableField(value = "content_type")
    private Integer contentType;

    /**
     * 抬头类型
     */
    @TableField(value = "header_type")
    private Integer headerType;

    /**
     * 抬头名称
     */
    @TableField(value = "header_name")
    private String headerName;

    /**
     * 公司税号
     */
    @TableField(value = "company_tax_no")
    private String companyTaxNo;

    /**
     * 注册手机
     */
    @TableField(value = "register_phone")
    private Integer registerPhone;

    /**
     * 注册地址
     */
    @TableField(value = "register_address")
    private String registerAddress;

    /**
     * 开户银行
     */
    @TableField(value = "open_bank")
    private String openBank;

    /**
     * 银行账号
     */
    @TableField(value = "bank_no")
    private String bankNo;

    /**
     * 手机
     */
    @TableField(value = "phone")
    private String phone;

    @TableField(value = "order_id")
    private Integer orderId;

    @TableField(value = "order_no")
    private String orderNo;

    @TableField(value = "state")
    private Integer state;

    @TableField(value = "user_id")
    private Integer userId;

    private static final long serialVersionUID = 1L;
}