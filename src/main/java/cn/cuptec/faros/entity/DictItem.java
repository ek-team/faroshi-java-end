package cn.cuptec.faros.entity;

import cn.cuptec.faros.common.annotation.Queryable;
import cn.cuptec.faros.common.enums.QueryLogical;
import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 字典项
 */
@Data
@ApiModel(value = "租户字典项")
public class DictItem extends Model<DictItem> {
	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "id", readOnly = true)
	@Queryable(queryLogical = QueryLogical.EQUAL)
	@TableId
	private Integer id;

	@ApiModelProperty(value = "字典id", required = true)
	@Queryable(queryLogical = QueryLogical.EQUAL)
	private Integer dictId;

	@ApiModelProperty(value = "字典项值", required = true)
	@Queryable(queryLogical = QueryLogical.LIKE)
	private String value;

	@ApiModelProperty(value = "标签值")
	@Queryable(queryLogical = QueryLogical.LIKE)
	private String label;

	//标签图标
	private String icon;

	@ApiModelProperty(value = "类型")
	@Queryable(queryLogical = QueryLogical.EQUAL)
	private String type;

	@ApiModelProperty(value = "描述")
	private String description;

	@ApiModelProperty(value = "排序(升序)")
	private Integer sort;

	@ApiModelProperty(value = "创建时间", readOnly = true)
	private LocalDateTime createTime;

	@ApiModelProperty(value = "更新时间", readOnly = true)
	@Queryable(queryLogical = QueryLogical.QUANTUM)
	private LocalDateTime updateTime;

	@ApiModelProperty(value = "备注")
	private String remarks;

	@ApiModelProperty(value = "删除标识", readOnly = true, hidden = true)
	@JSONField(deserialize = false, serialize = false)
	@TableLogic
	private String delFlag;

}
