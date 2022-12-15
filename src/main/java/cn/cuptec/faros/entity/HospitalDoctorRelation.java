package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.io.Serializable;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 医院 用户（医生） 关联
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "hospital_doctor_relation")
public class HospitalDoctorRelation extends Model<HospitalDoctorRelation> implements Serializable {

    /**
     * 医院id
     */
    @TableId(value = "hospital_id", type = IdType.INPUT)
    private Integer hospitalId;

    /**
     * 用户id 医生id
     */
    @TableId(value = "user_id", type = IdType.INPUT)
    private Integer userId;
    /**
     * 区分是医生还是用户 1-医生 2-用户
     */
    @TableId(value = "type", type = IdType.INPUT)
    private Integer type = 1;
    private static final long serialVersionUID = 1L;
}