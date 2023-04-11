package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 服务包回收规则
 */
@Data
public class RecyclingRule extends Model<RecyclingRule> {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer servicePackId;
    private Integer day; //天数
    private BigDecimal amount;//金额
}
