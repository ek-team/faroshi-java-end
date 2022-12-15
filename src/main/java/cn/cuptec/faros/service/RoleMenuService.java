package cn.cuptec.faros.service;

import cn.cuptec.faros.common.constrants.CacheConstants;
import cn.cuptec.faros.entity.RoleMenu;
import cn.cuptec.faros.mapper.RoleMenuMapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 角色菜单表 服务实现类
 * </p>
 */
@Service
@AllArgsConstructor
public class RoleMenuService extends ServiceImpl<RoleMenuMapper, RoleMenu> {
	private final CacheManager cacheManager;

    /**
	 * @param roleId  角色
	 * @param menuIds 菜单ID拼成的字符串，每个id之间根据逗号分隔
	 * @return
	 */
	@Transactional(rollbackFor = Exception.class)
	@CacheEvict(value = CacheConstants.MENU_DETAILS, key = "#roleId + '_menu'")
	public Boolean saveRoleMenus(Integer roleId, List<Integer> menuIds) {
		this.remove(Wrappers.<RoleMenu>query().lambda()
				.eq(RoleMenu::getRoleId, roleId));

		if (menuIds == null || menuIds.size() == 0) {
			return Boolean.TRUE;
		}
		List<RoleMenu> roleMenuList =
				//Arrays.stream(menuIds.split(CommonConstants.VALUE_SEPARATOR))
				menuIds.stream().map(menuId -> {
					RoleMenu roleMenu = new RoleMenu();
					roleMenu.setRoleId(roleId);
					roleMenu.setMenuId(Integer.valueOf(menuId));
					return roleMenu;
				}).collect(Collectors.toList());

		//清空userinfo
		cacheManager.getCache(CacheConstants.USER_DETAILS).clear();
		return this.saveBatch(roleMenuList);
	}
}
