package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

/**
 * 销售规格子类
 */
@Data
public class SaleSpecDesc extends Model<SaleSpecDesc> {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String name;
    private String remark;
    private Double deposit;//押金
    private Double rent;//租金
    private Double recoveryPrice;//回收价
    private Integer saleSpecId;
    private Integer status=0; //0-正常 1-删除
}
