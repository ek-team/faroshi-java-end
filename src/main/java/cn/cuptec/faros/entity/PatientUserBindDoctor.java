package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import javax.jws.soap.SOAPBinding;
import java.time.LocalDateTime;

/**
 * 就诊人和医生的绑定
 *
 */
@Data
public class PatientUserBindDoctor {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;//患者id
    private Integer patientUserId;//就诊人id
    private Integer doctorId;//医生id
    @TableField(exist = false)
    private User doctor;
    private Integer doctorTeamId;//医生团队id
    @TableField(exist = false)
    private DoctorTeam doctorTeam;

}
