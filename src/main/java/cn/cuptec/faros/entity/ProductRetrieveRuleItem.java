package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRetrieveRuleItem {

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private Integer productId;

    //时间区间-开始
    private Integer monthBegin;

    //时间区间-结束
    private Integer monthEnd;

    //是否可以回收
    private Boolean isRetrieveable;

    //回收价格
    private BigDecimal retrieveAmount;

}
