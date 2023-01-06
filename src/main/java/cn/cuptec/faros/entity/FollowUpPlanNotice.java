package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 随访计划通知类
 */
@Data
public class FollowUpPlanNotice extends Model<FollowUpPlanNotice> {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer followUpPlanId;//随访计划id
    private LocalDateTime noticeTime;//通知时间
    private Integer patientUserId;//通知用户id
    private Integer followUpPlanContentId;
    private Integer doctorId;
    private Integer totalPush;//总的推送次数
    private Integer push;//已推送次数
    @TableField(exist = false)
    private User user;
    @TableField(exist = false)
    private FollowUpPlanContent followUpPlanContent;
    @TableField(exist = false)
    private FollowUpPlan followUpPlan;
}
