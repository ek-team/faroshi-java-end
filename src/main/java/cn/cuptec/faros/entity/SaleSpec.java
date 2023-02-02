package cn.cuptec.faros.entity;

import cn.cuptec.faros.im.bean.ChatUserVO;
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
public class SaleSpec  extends Model<SaleSpec>  implements Comparable<SaleSpec> {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String name;
    private Integer sorts;//排序字段
    private Integer servicePackId;//服务包id
    @TableField(exist = false)
    private List<SaleSpecDesc> saleSpecDescs;

    private Integer oldId;
    @Override
    public int compareTo(SaleSpec o) {
        return this.sorts.compareTo(o.sorts);//升序
    }

}
