package cn.cuptec.faros.service;

import cn.cuptec.faros.common.constrants.CacheConstants;
import cn.cuptec.faros.common.exception.BizException;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.entity.User;
import cn.cuptec.faros.entity.UserRole;
import cn.cuptec.faros.mapper.UserRoleMapper;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * <p>
 * 用户角色表 服务实现类
 * </p>
 */
@Service
public class UserRoleService extends ServiceImpl<UserRoleMapper, UserRole> {

	@Autowired
	private UserService userService;

	/**
	 * 根据用户Id删除该用户的角色关系
	 */
	public Boolean deleteByUserId(Integer userId) {
		return baseMapper.deleteByUserId(userId);
	}

	public boolean deleteByUserIdAndRoleId(int userId, int roleId){
		User tenantUser = userService.getById(userId);
		if (tenantUser != null){
			boolean deleteUserRole = deleteUserRole(tenantUser.getPhone(), userId, roleId);
			return deleteUserRole;
		}else {
			throw new BizException(10000, new Object[]{userId});
		}
	}

	/**
	 * 获得部门下的用户id

	 * @return
	 */
	public List<Integer> getUserIdSByDeptIdAndRoleds(int deptId, List<Integer> roleIds){


		List<User> usersByDeptIdAndRoleds = getUsersByDeptIdAndRoleds(deptId, roleIds);
		if(CollUtil.isNotEmpty(usersByDeptIdAndRoleds))
			return usersByDeptIdAndRoleds.stream().map(User::getId).collect(Collectors.toList());


		return CollUtil.toList();
	}


	public List<User> getUsersByDeptIdAndRoleds(int deptId, List<Integer> roleIds){


		List<User> users = userService.list(Wrappers.<User>lambdaQuery()
				.in(User::getDeptId, deptId));
		if(CollUtil.isNotEmpty(users)){
			List<Integer> userIds = users.stream().map(User::getId)
					.collect(Collectors.toList());
			List<UserRole> userRoles = this.list(Wrappers.<UserRole>lambdaQuery()
					.in(UserRole::getRoleId, roleIds).in(UserRole::getUserId, userIds));
			if(CollUtil.isNotEmpty(userRoles)){
				List<Integer> collect = userRoles.stream().map(UserRole::getUserId).collect(Collectors.toList());

				return users.stream().filter(u -> collect.contains(u.getId())).collect(Collectors.toList());
			}
		}


		return CollUtil.toList();
	}


	@CacheEvict(value = CacheConstants.USER_DETAILS, key = "#username")
	public boolean deleteUserRole(String username, int userId, int roleId){
		UserRole tenantUserRole = new UserRole();
		tenantUserRole.setUserId(userId);
		tenantUserRole.setRoleId(roleId);
		return remove(Wrappers.query(tenantUserRole));
	}

	public List<Integer> listDistinctUserHasRole() {
		List<Integer> userIds = baseMapper.listDistinctUserHasRole();
		return userIds;
	}

	public List<UserRole> getListByUserId(Integer userID) {

		return  baseMapper.selectList(new QueryWrapper<UserRole>().lambda().eq(UserRole::getUserId,userID));
	}

	/**
	 * 判断用户是否是超级管理员
	 * @param userID
	 * @return
	 */
	public Boolean judgeUserIsAdmin(Integer userID) {

		List<UserRole> listByUserId = getListByUserId(userID);

		if (CollUtil.isNotEmpty(listByUserId)){
			List<Integer> collect = listByUserId.stream().map(UserRole::getRoleId).collect(Collectors.toList());
			if(collect.contains(7) || collect.contains(30) || collect.contains(29)){
				return true;
			}
		}
		return false;
	}

}
