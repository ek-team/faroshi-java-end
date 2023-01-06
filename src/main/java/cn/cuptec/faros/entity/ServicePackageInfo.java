package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.util.List;

/**
 * 服务包信息
 */
@Data
public class ServicePackageInfo extends Model<ServicePackageInfo> {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    private String name;
    private Integer type;//服务类型 1-图文咨询 2-电话咨询 3-康复指导
    private String image;//
    private Integer count;//服务次数
    private Integer expiredDay;//过期天数
    private String doctorTeamId;//医生团队id
    @TableField(exist = false)
    private List<Integer> doctorTeamIds;//医生团队id
    @TableField(exist = false)
    private List<DoctorTeam> doctorTeamList;
    private Integer formId;//表单id
    private Integer servicePackageId;
}
