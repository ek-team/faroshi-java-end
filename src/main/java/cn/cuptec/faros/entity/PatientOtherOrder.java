package cn.cuptec.faros.entity;

import cn.cuptec.faros.common.annotation.Queryable;
import cn.cuptec.faros.common.enums.QueryLogical;
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
    private Integer id;
    private Integer userId;
    private Integer deptId;
    private Integer chatUserId;
    private Integer hour;//过期时间
    private Integer doctorId;
    private Integer doctorTeamId;
    private LocalDateTime createTime;
    private Integer patientId;//就诊人id
    private String illnessDesc;//病情描述
    private String imageUrl;
    private Double amount;
    private Integer allergy;//过敏 1-有 2-无
    private Integer pastMedicalHistory;//过往病史1-有 2-无
    private Integer liverFunction;//肝功能1-有 2-无
    private Integer kidneyFunction;//肾功能1-有 2-无
    private Integer pregnancy;//备孕1-有 2-无
    private Integer type;//1-图文咨询申请
    //订单状态  1-待付款 2-已付款
    @Queryable(queryLogical = QueryLogical.EQUAL)
    private Integer status;
    private String orderNo;
}
