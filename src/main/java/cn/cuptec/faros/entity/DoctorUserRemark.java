package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

/**
 * 医生对患者的备注
 */
@Data
public class DoctorUserRemark extends Model<DoctorUserRemark> {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer doctorId;
    private Integer userId;
    private String remark;
}
