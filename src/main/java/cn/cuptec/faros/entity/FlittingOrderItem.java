package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

//调拨内容
@Data
public class FlittingOrderItem {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    //调拨单id
    private Integer flittingOrderId;

    //产品id
    private Integer productId;

    //调拨数量
    private Integer flitCount;
    //已调拨数量
    private Integer alreadyFlitCount;

    @TableField(exist = false)
    private String productName;

}
