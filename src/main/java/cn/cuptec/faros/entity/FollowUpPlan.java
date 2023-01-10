package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 随访计划
 */
@Data
public class FollowUpPlan extends Model<FollowUpPlan> {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer createUserId;
    private Integer deptId;
    private LocalDateTime createTime;
    private String name;
    private Integer joinType; //加入模式 1-自动加入 2-手动管理
    private Integer pushType;// 推送时间 1-首次加入推送 2-固定时间推送
    private Integer pushHour;//首次推送时
    private Integer pushMinute;//首次推送分
    private Integer lastPushHour;//后续推送时
    private Integer lastPushMinute;//后续推送分
    private Integer patientUserCount;//患者数量
    @TableField(exist = false)
    private List<FollowUpPlanContent> followUpPlanContentList;//随访计划内容
    @TableField(exist = false)
    private List<Integer> followUpPlanPatientUsers;//随访计划患者
    @TableField(exist = false)
    private List<FollowUpPlanPatientUser> followUpPlanPatientUserList;//随访计划患者
    @TableField(exist = false)
    private FollowUpPlanNoticeCount followUpPlanNoticeCount;
}
