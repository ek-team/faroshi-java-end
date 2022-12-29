package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.util.List;

/**
 * 销售规格
 */
@Data
public class SaleSpec extends Model<SaleSpec> {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String name;
    private Double deposit;//押金
    private Double rent;//租金
    private Double recoveryPrice;//回收价
    @TableField(exist = false)
    private List<SaleSpecDesc> saleSpecDescs;//销售规格选项
    private Integer deptId;
    private String remark;//备注
}
