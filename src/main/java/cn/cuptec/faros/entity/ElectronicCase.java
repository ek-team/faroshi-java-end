package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 电子病例
 */
@Data
public class ElectronicCase {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer patientId;
    private Integer createUserId;
    @TableField(exist = false)
    private String createUserName;
    private LocalDateTime createTime;
    private String title;//标题
    private String content;//详细内容
    private Double weight;//体重
    private double height;//身高
    private Integer sex;//性别 0-男 1-女
    private String name;//患者姓名
    private Integer inquiryCount;
    private Integer followUpPlanCount;
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    private LocalDateTime birthDay;//生日
    @TableField(exist = false)
    private List<Inquiry> inquirys;//问诊单信息
    @TableField(exist = false)
    private List<Integer> followUpPlanIds;//随访计划id
    private String followUpPlanIdList;//随访计划id
    @TableField(exist = false)
    private List<FollowUpPlan> followUpPlans;

}
