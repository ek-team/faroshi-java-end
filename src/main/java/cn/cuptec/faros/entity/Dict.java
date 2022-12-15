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

@Data
@ApiModel(value = "字典表")
public class Dict extends Model<Dict> {
	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "编号")
	@Queryable(queryLogical = QueryLogical.EQUAL)
	@TableId
	private Integer id;

	@ApiModelProperty(value = "类型")
	@Queryable(queryLogical = QueryLogical.EQUAL)
	private String type;

	@ApiModelProperty(value = "描述")
	private String description;

	@ApiModelProperty(value = "创建时间", readOnly = true)
	@Queryable(queryLogical = QueryLogical.QUANTUM)
	private LocalDateTime createTime;

	@ApiModelProperty(value = "更新时间", readOnly = true)
	private LocalDateTime updateTime;

	@ApiModelProperty(value = "是否系统内置", readOnly = true)
	private String system;

	@ApiModelProperty(value = "备注")
	private String remarks;

	@ApiModelProperty(value = "删除标记", readOnly = true, hidden = true)
	@JSONField(deserialize = false, serialize = false)
	@TableLogic
	private String delFlag;

}
