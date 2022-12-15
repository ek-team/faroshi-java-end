package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 角色菜单表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class RoleMenu extends Model<RoleMenu> {

	private static final long serialVersionUID = 1L;

	/**
	 * 角色ID
	 */
	@TableId(value = "role_id", type = IdType.INPUT)
	private Integer roleId;
	/**
	 * 菜单ID
	 */
	@TableId(value = "menu_id", type = IdType.INPUT)
	private Integer menuId;

}
