package cn.cuptec.faros.entity;

import cn.cuptec.faros.common.annotation.Queryable;
import cn.cuptec.faros.common.enums.QueryLogical;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 医生团队
 */
@Data
public class DoctorTeam extends Model<DoctorTeam> {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private Integer hospitalId;
    @TableField(exist = false)
    private String hospitalName;
    private String teamDesc;
    private Integer deptId;
    private Integer createUserId;
    private Integer status;//审核状态 0-待审核 -1审核通过 2-审核不通过
    private String checkDesc;//审核描述信息
    private LocalDateTime createTime;
    @TableField(exist = false)
    private List<DoctorTeamPeople> doctorTeamPeopleList;
}
