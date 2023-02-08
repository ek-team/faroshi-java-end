package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.util.List;

@Data
public class FormSetting extends Model<FormSetting> {

    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String text;
    private Integer formId;
    private String placeholder;//默认值
    private Integer type;//1-输入框 2-单选框 3-多行入框 4文本5图片 6 -多选框
    @TableField(exist = false)
    private List<FormOptions> formOptionsList;//选项
    private Integer isMust = 0;//是否必填 0-否 1-是

}
