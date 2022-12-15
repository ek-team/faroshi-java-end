package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.util.List;

/**
 * 产品规格
 */
@Data
public class ProductSpec extends Model<ProductSpec> {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String name;
    @TableField(exist = false)
    private List<ProductSpecDesc> productSpecDesc;//描述
    private Integer deptId;
}
