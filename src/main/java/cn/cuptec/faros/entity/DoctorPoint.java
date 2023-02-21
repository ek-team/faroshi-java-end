package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 医生积分 比如患者申请图文咨询 会给医生加积分 1比1
 */
@Data
public class DoctorPoint extends Model<DoctorPoint> {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private String pointDesc;
    private Double point; //单位 元
    private Integer doctorUserId;
    private Integer doctorTeamId;
    private LocalDateTime createTime;
    private Integer withdrawStatus;//提现状态 1-待提现0-已提现
    private String orderNo;
    @TableField(exist = false)
    private String doctorTeamName;
}
