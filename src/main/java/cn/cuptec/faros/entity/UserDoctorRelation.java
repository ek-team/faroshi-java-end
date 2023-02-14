package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import lombok.Data;

/**
 * 医生和患者的关系表
 */
@Data
@TableName(value = "user_doctor_relation")
public class UserDoctorRelation implements Serializable {
    @TableId(value = "user_id", type = IdType.INPUT)
    private Integer userId;


    private Integer doctorId;

    private static final long serialVersionUID = 1L;
}