package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 随访计划详细 内容 每天
 */
@Data
public class FollowUpPlanContent extends Model<FollowUpPlanContent> {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer followUpPlanId;//随访计划id
    private String notice;//提醒消息
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime day;
    private Integer dayAfter;//几天后，用于模板
    private Integer formId;//表单id
    private Integer status = 0;//0-正常执行 1-停止执行
    @TableField(exist = false)
    private Form form;//

}
