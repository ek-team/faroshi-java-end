package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.util.List;

/**
 * 规格组合
 */
@Data
public class SaleSpecGroup extends Model<SaleSpecGroup> {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String saleSpecIds;
    private String querySaleSpecIds;//查询值
    private Integer recovery;//0-回收 1-不回收
    private Double price;//售价
    private String remark;//备注
    private Double recoveryPrice;//回收价
    private String urlImage;//图片
    private Integer servicePackId;//服务包id
    @TableField(exist = false)
    private List<SaleSpecDesc> saleSpecDescList;//规格值信息
}
