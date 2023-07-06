package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * 快递的运单信息
 */
@Data
public class DeliveryInfo {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String deliverySn;
    private String deliveryName;
    private String userOrderNo;
    private String status;//快递状态
    private String message;
    private String taskId;
    private String courierName;//快递员姓名
    private String courierMobile;//快递员手机号
    private String label;//面单链接
    private Integer deptId;
    @TableField(exist = false)
    private UserOrder userOrder;
}
