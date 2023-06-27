package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 随访计划患者
 */
@Data
public class FollowUpPlanPatientUser extends Model<FollowUpPlanPatientUser> {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer followUpPlanId;//随访计划id
    private Integer userId;//患者id
    private LocalDateTime createTime;
    @TableField(exist = false)
    private User user;
}
