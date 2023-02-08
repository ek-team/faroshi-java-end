package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.util.List;

/**
 * 患者分组
 */
@Data
public class UserGroup {
    @TableId(type = IdType.AUTO)
    private Integer id;

    private String name;
    private Integer createUserId;//创建人id 医生id
    private Integer teamId;//团队id
    private Integer sort = 0;
    @TableField(exist = false)
    private Integer count;
    @TableField(exist = false)
    private List<Integer> userIds;
    @TableField(exist = false)
    private List<User> users;
}
