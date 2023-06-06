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
    private String day;
    private String dayStr;
    private Integer dayAfter;
    private Integer number;//数字几天几周后
    private Integer numberType;//1=立即提醒，2-天，3-周，4-月，5-年
    private Integer hour;//几小时后
    private Integer formId;//表单id
    private Integer articleId;//文章id
    private Integer status = 0;//0-正常执行 1-停止执行
    @TableField(exist = false)
    private Form form;//
    @TableField(exist = false)
    private Article article;
    @TableField(exist = false)
    private Integer addStatus = 0;//
    @TableField(exist = false)
    private LocalDateTime pushDay;

}
