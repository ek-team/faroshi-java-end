package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

/**
 * 问诊单
 */
@Data
public class Inquiry {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private String mainComplaint;//主诉
    private String imageUrl;//多个图片用，号分割
    private Integer allergy;//过敏 1-有 2-无
    private String allergyDesc;
    private Integer pastMedicalHistory;//过往病史1-有 2-无
    private String pastMedicalHistoryDesc;
    private Integer liverFunction;//肝功能1-有 2-无
    private Integer kidneyFunction;//肾功能1-有 2-无
    private Integer pregnancy;//备孕1-有 2-无
    private Integer electronicCaseId;//电子病例id

}

