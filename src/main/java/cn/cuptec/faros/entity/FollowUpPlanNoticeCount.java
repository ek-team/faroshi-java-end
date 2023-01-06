package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 推送计划次数记录
 */
@Data
public class FollowUpPlanNoticeCount extends Model<FollowUpPlanNoticeCount> {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer followUpPlanId;//随访计划id
    private Integer patientUserId;//通知用户id
    private Integer doctorId;
    private Integer totalPush;//总的推送次数
    private Integer push;//已经推送次数
}
