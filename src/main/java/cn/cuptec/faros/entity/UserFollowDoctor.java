package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * 用户和医生的好友关系
 */
@Data
public class UserFollowDoctor {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userId;
    private Integer doctorId;
    @TableField(exist = false)
    private User user;
}
