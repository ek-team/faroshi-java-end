package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

/**
 * 订单通知人员
 */
@Data
public class UserOrderNotify extends Model<UserOrderNotify> {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String userId;

}
