package cn.cuptec.faros.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;


@Data
@EqualsAndHashCode(callSuper = true)
//菜单权限表
public class Menu extends Model<Menu> {

	private static final long serialVersionUID = 1L;

	//菜单id
	@TableId(value = "id", type = IdType.INPUT)
	private Integer id;

	//菜单名称
	@NotBlank(message = "菜单名称不能为空")
	private String name;

	//菜单权限标识
	private String permission;

	//菜单父ID
	@NotNull(message = "菜单父ID不能为空")
	private Integer parentId;

	//菜单图标
	private String icon;

	//前端url
	private String path;

	//排序
	private Integer sort;

	//菜单类型 （0菜单 1按钮）
	@NotNull(message = "菜单类型不能为空")
	private String type;

	//路由缓冲
	private String keepAlive;

	//创建时间
	private LocalDateTime createTime;

	//更新时间
	private LocalDateTime updateTime;

	@ApiModelProperty(value = "删除标识 0--正常 1--删除", readOnly = true, hidden = true)
	@JSONField(deserialize = false, serialize = false)
	@TableLogic
	private String delFlag;

}
