package cn.cuptec.faros.entity;

import cn.cuptec.faros.im.bean.ChatUserVO;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 服务包续租规则
 */
@Data
public class RentRule extends Model<RentRule> implements Comparable<RentRule> {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer servicePackId;
    private Integer day; //天数
    private BigDecimal amount;//金额
    private Integer serviceCount;//图文咨询次数

    @Override
    public int compareTo(RentRule o) {
        return this.amount.compareTo(o.amount);//根据时间降序
    }
}
