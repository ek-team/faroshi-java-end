package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * 用户和分组的关系表
 */
@Data
public class UserGroupRelationUser {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer userGroupId;
    private Integer userId;
}
