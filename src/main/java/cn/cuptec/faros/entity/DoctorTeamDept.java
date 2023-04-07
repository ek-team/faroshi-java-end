package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

/**
 * 医生团队和代理商的关系
 */
@Data
public class DoctorTeamDept extends Model<DoctorTeamDept> {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer teamId;

    private Integer deptId;
    @TableField(exist = false)
    private Dept dept;

}
