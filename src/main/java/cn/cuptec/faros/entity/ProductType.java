package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * @Description:
 * @Author mby
 * @Date 2021/8/24 16:27
 */
@Data
@TableName("product_type")
public class ProductType {
    @TableId(type = IdType.AUTO)
    private int id;
    private String name;
    //0禁用 1正常
    private int status = 1;
}
