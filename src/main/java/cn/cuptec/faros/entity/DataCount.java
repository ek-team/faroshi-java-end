package cn.cuptec.faros.entity;

import cn.cuptec.faros.common.annotation.Queryable;
import cn.cuptec.faros.common.enums.QueryLogical;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

@Data
public class DataCount {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer totalCollected;
    private Integer monthlyCollected;
    private Integer totalRecoveryNum;
    private Integer monthlyRecoveryNum;
    private Integer  totalHomeRecoveryNum;
    private Integer  monthlyHomeRecoveryNum;
}
