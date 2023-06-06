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
    private Integer deptId;//记录部门下的随访模板
    private LocalDateTime createTime;
    private Integer serviceDay;//计划周期
    private String name;
    private String optionName; //填写的手术名称
    private Integer createType=0;//0 个人，1 公用
    private Integer patientUserCount;//患者数量
    private Integer teamId;//团队id
    @TableField(exist = false)
    private List<FollowUpPlanContent> followUpPlanContentList;//随访计划内容
    @TableField(exist = false)
    private List<Integer> followUpPlanPatientUsers;//随访计划患者
    @TableField(exist = false)
    private List<FollowUpPlanPatientUser> followUpPlanPatientUserList;//随访计划患者
    @TableField(exist = false)
    private FollowUpPlanNoticeCount followUpPlanNoticeCount;
}
