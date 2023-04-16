package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户举报记录 建议
 */
@Data
public class ReportRecord extends Model<ReportRecord> {
    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private String reportDesc;
    private Integer category;//1-举报
    private LocalDateTime createTime;
}
