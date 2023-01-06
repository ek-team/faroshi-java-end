package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

/**
 * 医生团对病种 关联
 */
@Data
public class DoctorTeamDiseases extends Model<DoctorTeamDiseases> {

    @TableId(type = IdType.AUTO)
    private String id;
    private Integer teamId;
    private Integer diseasesId;
}
