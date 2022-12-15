package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

/**
 * 表单选项
 */
@Data
public class FormOptions extends Model<FormOptions> {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer formId;
    private Integer formSettingId;
    private String text;
}
