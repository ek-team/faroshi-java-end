package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * 患者和团队的关系
 */
@Data
public class PatientRelationTeam {
    @TableId(type = IdType.AUTO)
    private String id;
    private Integer patientId;
    private Integer teamId;

}
