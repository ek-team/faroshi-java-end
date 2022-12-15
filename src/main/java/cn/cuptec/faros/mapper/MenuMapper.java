package cn.cuptec.faros.mapper;

import cn.cuptec.faros.entity.Menu;
import cn.cuptec.faros.vo.MenuVO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * 菜单权限表 Mapper 接口
 * </p>
 */
public interface MenuMapper extends BaseMapper<Menu> {

	/**
	 * 通过角色编号查询菜单
	 *
	 * @param roleId 角色ID
	 * @return
	 */
	@Select("SELECT menu.* FROM menu LEFT JOIN role_menu ON menu.id = role_menu.menu_id " +
			"WHERE menu.del_flag = 0 AND role_menu.role_id = #{roleId} " +
			"ORDER BY menu.sort DESC")
	List<MenuVO> listMenusByRoleId(Integer roleId);

	/**
	 * 通过角色ID查询权限
	 *
	 * @param roleIds Ids
	 * @return
	 */
	@Select("SELECT m.permission FROM menu m, role_menu rm WHERE m.id = rm.menu_id AND m.del_flag = 0 AND rm.role_id IN (#{roleIds})")
	List<String> listPermissionsByRoleIds(String roleIds);
}
