package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

/**
 * 医生可以设置哪些服务
 */
@Data
public class DoctorUserServiceSetUp extends Model<DoctorUserServiceSetUp> {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String category;//服务类型 图文咨询 电话咨询 团队图文咨询

    private String desc;//描述
    @TableField(exist = false)
    private DoctorUserAction doctorUserAction;//已开通的服务
}
