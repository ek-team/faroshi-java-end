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
    private Integer day;//天数
    private Double rent;//总价
    private Double recoveryPrice;//回收价
    private Integer deptId;
    private String remark;//备注
    private Integer servicePackId;//服务包id
    private Integer type;//0-租用 1-购买
    private Integer status = 0;//0-正常 1-购买
}
