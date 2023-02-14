package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户填写的表单数据
 */
@Data
public class FormUserData extends Model<FormUserData> {

    @TableId(type = IdType.AUTO)
    private Integer id;

    private Integer orderId;
    private Integer str;
    private Integer userId;
    private Integer doctorId;
    private Integer formId;//表单id

    private Integer formSettingId;//题目id
    private String type;//1-输入框 2-单选框 3-输入框 4-下拉框 5-日期 6 -多选框
    private Object answer;//答案
    private LocalDateTime createTime;
    private Double scope;
    private String groupId;
}
