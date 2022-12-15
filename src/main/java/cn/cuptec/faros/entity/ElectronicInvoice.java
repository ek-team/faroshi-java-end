package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class ElectronicInvoice  {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "发票类型 1电子普通发票")
    private int category;

    @ApiModelProperty(value = "发票内容")
    private String content;

    @ApiModelProperty(value = "抬头类型 1个人 2单位")
    private int lookUp;

    @ApiModelProperty(value = "抬头名称")
    private String lookUpName;

    @ApiModelProperty(value = "手机号码")
    private String mobile;

    @ApiModelProperty(value = "单位税号")
    private String taxNum;

    @ApiModelProperty(value = "业务员id")
    private Integer salesmanId;

    @ApiModelProperty(value = "用户Id")
    private Integer userId;

    @ApiModelProperty(value = "发票金额")
    private Double amount;

    @ApiModelProperty(value = "收货地址")
    private String receiptAddress;

    @ApiModelProperty(value = "设备序列号")
    private String productSn;

    @ApiModelProperty(value = "发票申请状态 1申请中")
    private int status;

    @ApiModelProperty(value = "订单id")
    private int orderId;
}
