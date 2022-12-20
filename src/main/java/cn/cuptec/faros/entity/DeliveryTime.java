package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * 期望发货时间选择
 */
@Data
public class DeliveryTime {
    @TableId
    private Integer id;
}
