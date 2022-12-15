package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 表单管理
 */
@Data
public class Form extends Model<Form> {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private String title;
    private LocalDateTime createTime;
    private Integer deptId;
    private Integer createUserId;
    private List<FormSetting> formSettings;
    private Integer status=0;//0-正常 1-作废
}
