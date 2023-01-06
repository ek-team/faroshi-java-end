package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

/**
 * 随访计划详细 内容 每天
 */
@Data
public class FollowUpPlanContent extends Model<FollowUpPlanContent> {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer followUpPlanId;//随访计划id
    private String day;//几天后
    private String notice;//提醒消息
    private Integer formId;//表单id
    @TableField(exist = false)
    private Form form;//
}
