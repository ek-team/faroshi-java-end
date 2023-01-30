package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

/**
 * 服务包病种关联
 */
@Data
public class ServicePackDiseases extends Model<ServicePackDiseases> {

    @TableId(type = IdType.AUTO)
    private String id;
    private Integer servicePackId;
    private Integer diseasesId;
}
