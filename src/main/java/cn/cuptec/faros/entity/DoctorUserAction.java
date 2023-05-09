package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;


/**
 * 医生设置自己的功能图文咨询、团队图文咨询、电话咨询、专家解读、团队专家解读的开关
 */
@Data
public class DoctorUserAction extends Model<DoctorUserAction> {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer userId;//医生id
    private Integer teamId;//团队id
    private Integer doctorUserServiceSetUpId;
    private Integer hour;//过期时间
    private Double price;
    private Integer count;//接单数量限制
    private Integer status=0; //0-正常 1-关闭
}
