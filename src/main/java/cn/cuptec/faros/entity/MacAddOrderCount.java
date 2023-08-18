package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * mac地址设备订单数量
 */
@Data
public class MacAddOrderCount {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer count;//数量
    private String macAdd;
}
