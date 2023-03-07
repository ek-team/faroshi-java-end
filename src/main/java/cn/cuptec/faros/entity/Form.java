package cn.cuptec.faros.entity;

import cn.cuptec.faros.common.annotation.Queryable;
import cn.cuptec.faros.common.enums.QueryLogical;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
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
    @Queryable(queryLogical = QueryLogical.LIKE)
    private String title;
    private LocalDateTime createTime;
    private Integer deptId;
    private Integer createId;//创建人用户id
    private Integer createUserId;
    @TableField(exist = false)
    private List<FormSetting> formSettings;
    private Integer status=0;//0-正常 1-作废
    @TableField(exist = false)
    private List<FormUserData> formUserDataList;
    @TableField(exist = false)
    private Double scope;
    @TableField(exist = false)
    private String userName;
    @TableField(exist = false)
    private String doctorName;
    @TableField(exist = false)
    private String groupId;
    private Integer platform; //1-后台创建
}
