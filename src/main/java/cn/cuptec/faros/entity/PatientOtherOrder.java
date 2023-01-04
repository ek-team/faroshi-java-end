package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 患者其它订单 比如图文咨询申请等
 */
@Data
public class PatientOtherOrder {
    @TableId(type = IdType.AUTO)
    private String id;
    private Integer userId;
    private Integer doctorId;
    private Integer doctorTeamId;
    private LocalDateTime createTime;
    private Integer patientId;//就诊人id
    private Integer illnessDesc;//病情描述
    private String imageUrl;
    private Double amount;
    private Integer type;//1-图文咨询申请
}
