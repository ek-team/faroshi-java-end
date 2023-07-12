package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * 设备用户的其他信息 根据身份证关联
 */
@Data
public class PlanUserOtherInfo {
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String idCard;
    private String registrationEvaluation;//手术评估
    private String bodyPartName;//手术部位
    private String secondDiseaseName;//疾病名称

}
