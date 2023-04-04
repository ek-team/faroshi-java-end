package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 订单修改记录
 */
@Data
public class UpdateOrderRecord {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer orderId;

    private LocalDateTime createTime;

    private Integer createUserId;

    private String descStr;
}
