package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 随访计划推送时间
 */
@Data
public class FollowUpPlanContentTime extends Model<FollowUpPlanContentTime> {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer followUpPlanId;//随访计划id
    private Integer followUpPlanContentId;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private LocalDateTime time;
}
