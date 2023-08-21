package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * 设备用户数量
 */
@Data
public class ProductStockUserMacAddCount {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer count;
    private String macAdd;
}
