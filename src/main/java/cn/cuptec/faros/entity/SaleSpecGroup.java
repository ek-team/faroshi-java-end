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
    private Integer stock;//库存
    private Integer status = 0;//0-正常 1-禁用
    private String weight;//回收重量
    private String recycleRemark;//回收备注
    private Integer serviceCount;//图文咨询次数
    private Integer sendUrl=0;//是否发送支架url 0-不发送 1-发送
    @TableField(exist = false)
    private List<SaleSpecDesc> saleSpecDescList;//规格值信息
}
