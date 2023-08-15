package cn.cuptec.faros.entity;

import cn.cuptec.faros.common.annotation.Queryable;
import cn.cuptec.faros.common.enums.QueryLogical;
import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门管理
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class Dept extends Model<Dept> {

    private static final long serialVersionUID = 1L;

    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    /**
     * 部门名称
     */
    @NotBlank(message = "部门名称不能为空")
    @Queryable(queryLogical = QueryLogical.LIKE)
    private String name;

    /**
     * 排序值
     */
    private Integer sort;
    private String subMchId;
    private String phone;//代理商设置的联系电话
    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    @ApiModelProperty(value = "修改时间", readOnly = true)
    private LocalDateTime updateTime;

    @ApiModelProperty(value = "父级部门id")
    @Queryable(queryLogical = QueryLogical.EQUAL)
    private Integer parentId;

    /**
     * 是否已删除
     */
    @JSONField(deserialize = false, serialize = false)
    @TableLogic
    private String delFlag;

    @ApiModelProperty(value = "父级部门名称")
    @Queryable(queryLogical = QueryLogical.EQUAL)
    @TableField(exist = false)
    private Integer parentName;

    @TableField(exist = false)
    private List<Integer> deptCityIds;

}
