package cn.cuptec.faros.service;

import cn.cuptec.faros.common.constrants.CacheConstants;
import cn.cuptec.faros.entity.Menu;
import cn.cuptec.faros.entity.Role;
import cn.cuptec.faros.entity.RoleMenu;
import cn.cuptec.faros.entity.UserRole;
import cn.cuptec.faros.mapper.RoleMapper;
import cn.cuptec.faros.mapper.RoleMenuMapper;
import cn.hutool.core.lang.UUID;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleService extends ServiceImpl<RoleMapper, Role> {
	@Resource
	private UserRoleService userRoleService;
	@Resource
    private RoleMenuService roleMenuService;
	@Resource
	private MenuService menuService;

    /**
     * 保存角色信息及角色菜单
     * @param role
     * @return
     */
    @Override
    @Transactional
    public boolean save(Role role) {
        super.save(role);
        if (role.getMenuIds() != null){
            roleMenuService.saveRoleMenus(role.getId(), role.getMenuIds());
        }
        return Boolean.TRUE;
    }

    @Override
    @Transactional
    public boolean updateById(Role role){
        super.updateById(role);
        if (role.getMenuIds() != null)
            roleMenuService.saveRoleMenus(role.getId(), role.getMenuIds());
        return Boolean.TRUE;
    }

    /**
	 * 通过用户ID，查询角色信息
	 *
	 * @param userId
	 * @return
	 */
	public List<Role> findRolesByUserId(Integer userId) {
		return baseMapper.listRolesByUserId(userId);
	}

    public Role getRoleByCode(String code) {
        LambdaQueryWrapper wrapper = new QueryWrapper<Role>()
                .lambda()
                .eq(Role::getRoleCode,code);



	    return this.getOne(wrapper);
    }


	/**
	 * 通过角色ID，删除角色,并清空角色菜单缓存
	 *
	 * @param id
	 * @return
	 */
	@CacheEvict(value = CacheConstants.CLIENT_DETAILS_KEY, allEntries = true)
	@Transactional(rollbackFor = Exception.class)
	public Boolean removeRoleById(Integer id) {
		roleMenuService.remove(Wrappers
			.<RoleMenu>update().lambda()
			.eq(RoleMenu::getRoleId, id));
		return this.removeById(id);
	}

	public List<Menu> listMenusByRoleIds(Collection<Integer> roleIds){
	    if (roleIds.size() == 0){
	        return new ArrayList<>();
        }
        Collection<RoleMenu> roleMenus = roleMenuService.listByIds(roleIds);
	    List<Integer> menuIds = new ArrayList<>();
	    roleMenus.forEach(roleMenu -> menuIds.add(roleMenu.getMenuId()));
	    if (menuIds.size() > 0)
            return (List<Menu>) menuService.listByIds(menuIds);
	    else
	        return new ArrayList<>();
    }

	/**
	 * 根据角色id查询用户id集合
	 * @param roleId
	 * @return
	 */
	public List<Integer> listRoleUserIds(int roleId) {
		List<UserRole> tenantUserRoles = userRoleService.list(Wrappers.<UserRole>lambdaQuery().eq(UserRole::getRoleId, roleId));
		List<Integer> userIds = new ArrayList<>();
		tenantUserRoles.forEach(sysUserRole ->
			userIds.add(sysUserRole.getUserId())
		);
		return userIds;
	}

    /**
     * 获取角色列表及每个角色包含的用户数量
     * @param isSystem 是否包含系统内置角色
     * @return
     */
    public List<Role> listCascade(Boolean isSystem) {
        LambdaQueryWrapper<Role> tenantRoleLambdaQueryWrapper = Wrappers.<Role>lambdaQuery();
        if (isSystem != null)
            tenantRoleLambdaQueryWrapper.eq(Role::isSystem, isSystem);
        List<Role> tenantRoles = list(tenantRoleLambdaQueryWrapper);
        tenantRoles.forEach(role -> {
            int count = userRoleService.count(
                    Wrappers.<UserRole>lambdaQuery().eq(UserRole::getRoleId, role.getId())
            );
            role.setUserCount(count);
        });
        return tenantRoles;
    }

    /**
     * 将管理员角色的菜单重置
     */
    @Transactional
    public void initAdministratorMenu() {
        List<Menu> list = menuService.list();
        roleMenuService.remove(Wrappers.<RoleMenu>lambdaQuery()
                .eq(RoleMenu::getRoleId, 7)
        );
        List<RoleMenu> roleMenus = list.stream().map(menu -> {
            RoleMenu roleMenu = new RoleMenu();
            roleMenu.setMenuId(menu.getId());
            roleMenu.setRoleId(7);
            return roleMenu;
        }).collect(Collectors.toList());
        roleMenuService.saveBatch(roleMenus);
    }
}
