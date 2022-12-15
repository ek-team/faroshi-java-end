package cn.cuptec.faros.entity;

import cn.cuptec.faros.common.annotation.Queryable;
import cn.cuptec.faros.common.enums.QueryLogical;
import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

//角色
@Data
@EqualsAndHashCode(callSuper = true)
public class Role extends Model<Role> {

	private static final long serialVersionUID = 1L;

	@TableId(value = "id", type = IdType.AUTO)
	private Integer id;

	//角色名称
	@Queryable(queryLogical = QueryLogical.EQUAL)
	@NotBlank(message = "角色名称 不能为空")
	private String roleName;

	//角色标识
	private String roleCode;

	//角色描述
	private String roleDesc;

	//数据权限类型 0-全部 1-自定义 2-本级及下级 3-本级
	@NotNull(message = "数据权限类型 不能为空")
	private Integer dsType;

	//数据权限,自定义时，传入部门id集合
	private String dsScope;

	//创建时间
	@Queryable(queryLogical = QueryLogical.QUANTUM)
	private LocalDateTime createTime;

	//更新时间
	private LocalDateTime updateTime;

	//是否是系统默认
	private boolean system;

	/**
	 * 删除标识（0-正常,1-删除）
	 */
	@TableLogic
	@JSONField(deserialize = false, serialize = false)
	private String delFlag;

	//包含的用户数量
	@TableField(exist = false)
	private Integer userCount;

    /**
     * 角色权限id集合
     */
    @TableField(exist = false)
	private List<Integer> menuIds;

	@Override
	protected Serializable pkVal() {
		return this.id;
	}

}
