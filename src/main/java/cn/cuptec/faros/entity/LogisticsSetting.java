package cn.cuptec.faros.entity;

import cn.cuptec.faros.common.annotation.Queryable;
import cn.cuptec.faros.common.enums.QueryLogical;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * 物流设置
 */
@Data
public class LogisticsSetting {
    @Queryable(queryLogical = QueryLogical.EQUAL)
    @TableId(type = IdType.UUID)
    private String id;

    private Integer deptId;

    private String logistics;
}
