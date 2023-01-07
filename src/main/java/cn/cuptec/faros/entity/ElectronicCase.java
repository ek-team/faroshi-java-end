package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 电子病例
 */
@Data
public class ElectronicCase {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer patientId;
    private Integer createUserId;
    private LocalDateTime createTime;
    private String title;//标题
    private Double weight;//体重
    private double height;//身高

}
