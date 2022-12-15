package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Description:支持产品列表
 * @Author mby
 * @Date 2021/8/26 10:31
 */
@Data
@TableName("`support_product_list`")
public class SupportProductList {
    @TableId(type = IdType.AUTO)
    private int id;
    private int depId;
    private int productId;
    @TableField(exist = false)
    private String productName;
}
