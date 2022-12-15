package cn.cuptec.faros.service;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.common.constrants.CacheConstants;
import cn.cuptec.faros.entity.Menu;
import cn.cuptec.faros.entity.RoleMenu;
import cn.cuptec.faros.mapper.MenuMapper;
import cn.cuptec.faros.mapper.RoleMenuMapper;
import cn.cuptec.faros.vo.MenuVO;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 * 菜单权限表 服务实现类
 * </p>
 */
@Service
@AllArgsConstructor
public class MenuService extends ServiceImpl<MenuMapper, Menu> {
	private final RoleMenuMapper tenantRoleMenuMapper;

	@Cacheable(value = CacheConstants.MENU_DETAILS, key = "#roleId  + '_menu'", unless = "#result == null")
	public List<MenuVO> findMenuByRoleId(Integer roleId) {
		return baseMapper.listMenusByRoleId(roleId);
	}

	@Transactional(rollbackFor = Exception.class)
	@CacheEvict(value = CacheConstants.MENU_DETAILS, allEntries = true)
	public RestResponse removeMenuById(Integer id) {
		// 查询父节点为当前节点的节点
		List<Menu> menuList = this.list(Wrappers.<Menu>query()
				.lambda().eq(Menu::getParentId, id));
		if (CollUtil.isNotEmpty(menuList)) {
			return RestResponse.failed("菜单含有下级不能删除");
		}

		tenantRoleMenuMapper
				.delete(Wrappers.<RoleMenu>query()
						.lambda().eq(RoleMenu::getMenuId, id));

		//删除当前菜单及其子菜单
		return RestResponse.ok(this.removeById(id));
	}

	@CacheEvict(value = CacheConstants.MENU_DETAILS, allEntries = true)
	public Boolean updateMenuById(Menu tenantMenu) {
		return this.updateById(tenantMenu);
	}
}
