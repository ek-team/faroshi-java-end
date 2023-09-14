package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * 通知记录 支付 医嘱消息
 */
@Data
public class Notice {
    @TableId(type = IdType.AUTO)
    private String id;
    private String context;
}
