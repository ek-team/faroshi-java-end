package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * 医生病种 关联表
 */
@Data
public class PatientUserDiseases {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;//患者id
    private Integer diseasesId;
    private Integer diseasesName;
}
