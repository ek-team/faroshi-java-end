package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

/**
 * 医生团队人员
 */
@Data
public class DoctorTeamPeople extends Model<DoctorTeamPeople> {

    @TableId(type = IdType.AUTO)
    private String id;
    private Integer userId;
    @TableField(exist = false)
    private String userName;
    private Integer teamId;
}
