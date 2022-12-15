package cn.cuptec.faros.service;

import cn.cuptec.faros.common.bean.TreeSortDto;
import cn.cuptec.faros.common.constrants.CommonConstants;
import cn.cuptec.faros.common.constrants.SecurityConstants;
import cn.cuptec.faros.common.enums.DataScopeTypeEnum;
import cn.cuptec.faros.common.exception.CheckedException;
import cn.cuptec.faros.common.utils.DeptTreeUtil;
import cn.cuptec.faros.common.utils.StringUtils;
import cn.cuptec.faros.config.security.service.CustomUser;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.entity.Dept;
import cn.cuptec.faros.entity.DeptCity;
import cn.cuptec.faros.entity.DeptRelation;
import cn.cuptec.faros.entity.Role;
import cn.cuptec.faros.mapper.DeptMapper;
import cn.cuptec.faros.vo.DeptTree;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import javax.annotation.Resource;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 部门管理 服务实现类
 */
@Service
@AllArgsConstructor
public class DeptService extends ServiceImpl<DeptMapper, Dept> {
	private final DeptRelationService deptRelationService;
	private final DeptCityService deptCityService;
	private final RoleService roleService;




	private final UserRoleService userRoleService;

    @Override
    public Dept getById(Serializable id) {
        Dept dept = super.getById(id);
        Assert.notNull(dept, "不存在部门id为" + id + "的数据");
        List<Integer> cityIds = deptCityService.list(Wrappers.<DeptCity>lambdaQuery()
                .select(DeptCity::getCityId)
                .eq(DeptCity::getDeptId, id)
        ).stream().map(deptCity -> deptCity.getCityId()).collect(Collectors.toList());
        dept.setDeptCityIds(cityIds);
        return dept;
    }

    /**
	 * 添加信息部门
	 * @param dept 部门
	 * @return
	 */
	@Transactional(rollbackFor = Exception.class)
	public Boolean saveDept(Dept dept) {
		if(dept.getParentId() == null){
			dept.setParentId(CommonConstants.TREE_ROOT_ID);
		}
		this.save(dept);
		deptRelationService.insertDeptRelation(dept);

        List<DeptCity> deptCities = dept.getDeptCityIds().stream().map(cityId -> {
            DeptCity deptCity = new DeptCity();
            deptCity.setCityId(cityId);
            deptCity.setDeptId(dept.getId());
            return deptCity;
        }).collect(Collectors.toList());

        if (deptCities.size() > 0){
            deptCityService.saveBatch(deptCities);
        }

		return Boolean.TRUE;
	}

	/**
	 * 删除部门
	 * @param id 部门 ID
	 * @return 成功、失败
	 */
	@Transactional(rollbackFor = Exception.class)
	public Boolean removeDeptById(Integer id) {
		//级联删除部门
		List<Integer> idList = deptRelationService
				.list(Wrappers.<DeptRelation>query().lambda()
						.eq(DeptRelation::getAncestor, id))
				.stream()
				.map(DeptRelation::getDescendant)
				.collect(Collectors.toList());

		if (CollUtil.isNotEmpty(idList)) {
			this.removeByIds(idList);
		}

		//删除部门级联关系
		deptRelationService.deleteAllDeptRealtion(id);

		//删除部门与城市对应关系
        deptCityService.remove(Wrappers.<DeptCity>lambdaQuery()
                .eq(DeptCity::getDeptId, id)
        );

		return Boolean.TRUE;
	}

	/**
	 * 更新部门
	 * @param dept 部门信息
	 * @return 成功、失败
	 */
	@Transactional(rollbackFor = Exception.class)
	public Boolean updateDeptById(Dept dept) {
		//更新部门状态
		this.updateById(dept);
		//更新部门关系
		DeptRelation relation = new DeptRelation();
		relation.setAncestor(dept.getParentId());
		relation.setDescendant(dept.getId());
		deptRelationService.updateDeptRealtion(relation);
		//更新部门城市关系
        deptCityService.updateDeptCityRelation(dept.getId(), dept.getDeptCityIds());
		return Boolean.TRUE;
	}

	/**
	 * 查询全部部门树
	 * @return 树
	 */
	public List<DeptTree> getTree() {
		return getDeptTree(this.list(Wrappers.emptyWrapper()));
	}

    /**
	 * 构建部门树
	 * @param depts 部门
	 * @return
	 */
	private List<DeptTree> getDeptTree(List<Dept> depts) {
		List<DeptTree> treeList = getAllDeptTree(depts);
		return DeptTreeUtil.build(treeList, CommonConstants.TREE_ROOT_ID);
	}

	/**
	 * 根据id获取子级部门列表
	 * @param deptId
	 * @return
	 */
	public List<DeptTree> selectTree(int deptId) {
		List<Dept> depts = list();
		List<DeptTree> treeList = getAllDeptTree(depts);
		List<DeptTree> deptTrees = DeptTreeUtil.build(treeList, deptId);
		return deptTrees;
	}

	/**
	 * 只获取当前部门的子集（下一级）列表
	 * @param deptId
	 * @return
	 */
	public List<Dept> getSubList(int deptId) {

		return super.list(Wrappers.<Dept>lambdaQuery().eq(Dept::getParentId, deptId));
	}

	public List<Dept> getMySubListByIsAdmin() {
		CustomUser user = SecurityUtils.getUser();
		Boolean isAdmin = userRoleService.judgeUserIsAdmin(user.getId());
		Integer deptId =   isAdmin? 1:user.getDeptId();

		List<Dept> subList = this.getSubList(deptId);
		if(isAdmin && CollUtil.isNotEmpty(subList)){
			subList = this.getSubList(subList.stream().map(Dept::getId).collect(Collectors.toList()));
		}



		return CollUtil.isNotEmpty(subList)?subList:CollUtil.toList();
	}

	public List<Dept> getSubList(List<Integer> deptIds) {

		return super.list(Wrappers.<Dept>lambdaQuery().in(Dept::getParentId, deptIds));
	}

	private List<DeptTree> getAllDeptTree(List<Dept> depts){
		return depts.stream()
				.filter(dept -> !dept.getId().equals(dept.getParentId()))
				.sorted(Comparator.comparingInt(Dept::getSort))
				.map(dept -> {
					DeptTree node = new DeptTree();
					node.setId(dept.getId());
					node.setParentId(dept.getParentId());
					node.setName(dept.getName());
					return node;
				}).collect(Collectors.toList());
	}

	private static List<DeptTree> listDeepChildren(String rootNameStr, List<DeptTree> treeNodes){
		List<DeptTree> array = new ArrayList<>();
		treeNodes.forEach((DeptTree treeNode) -> {
			String tempRootNameStr = rootNameStr;
			if (!StringUtils.isEmpty(tempRootNameStr)){
				tempRootNameStr = tempRootNameStr + CommonConstants.CONCATENATION_CHARACTER + treeNode.getName();
			}else {
				tempRootNameStr = treeNode.getName();
			}
			if (treeNode.getChildren() == null || treeNode.getChildren().isEmpty()){
				treeNode.setName(tempRootNameStr);
				array.add(treeNode);
			}else {
				List<DeptTree> tempDeptTrees = new ArrayList<>();
				treeNode.getChildren().forEach((DeptTree deptTree) -> {
					tempDeptTrees.add(deptTree);
				});
				array.addAll(listDeepChildren(tempRootNameStr, tempDeptTrees));
			}
		});
		return array;
	}

	/**
	 * 批量编辑
	 * @param depts
	 * @return
	 */
	@Transactional
	public boolean updateBatch(List<Dept> depts) {
		Assert.notEmpty(depts, "请选择要编辑的数据");
		super.updateBatchById(depts);
		List<DeptRelation> tenantDeptRelations = new ArrayList<>();
		depts.forEach(sysDept -> {
			DeptRelation relation = new DeptRelation();
			relation.setAncestor(sysDept.getParentId());
			relation.setDescendant(sysDept.getId());
			tenantDeptRelations.add(relation);
		});
		if(tenantDeptRelations.size() > 0)
			deptRelationService.updateBatchById(tenantDeptRelations);
		//更新部门城市关系表
		depts.forEach(dept -> deptCityService.updateDeptCityRelation(dept.getId(), dept.getDeptCityIds()));
		return true;
	}

	/**
	 * 编辑排序值
	 */
	public void editSort(TreeSortDto treeSortDto) {
		int count = count(Wrappers.<Dept>lambdaQuery().eq(Dept::getParentId, treeSortDto.getParentId()));
		Assert.isTrue(count == treeSortDto.getNodeIds().size(), "节点数量不一致");
		baseMapper.autoSort(treeSortDto.getNodeIds());
	}


	public Dept getParentDept(Integer deptId) {
		if(deptId !=null){
			Dept byId = this.getById(deptId);
			if(byId !=null)  return this.getById(byId.getParentId());
		}

		return null;

	}

	/**
	 * 根据数据权限获取部门树
	 * @return
	 */
	public List<DeptTree> treeScoped() {
		CustomUser user = SecurityUtils.getUser();
		if (user == null) {
			throw new CheckedException("auto datascope, set up security details true");
		}

		List<String> roleIdList = user.getAuthorities()
				.stream().map(GrantedAuthority::getAuthority)
				.filter(authority -> authority.startsWith(SecurityConstants.ROLE))
				.map(authority -> authority.split("_")[1])
				.collect(Collectors.toList());

		Role role = roleService.list(Wrappers.<Role>lambdaQuery().in(Role::getId, roleIdList))
				.stream().min(Comparator.comparingInt(o -> o.getDsType())).get();
		Integer dsType = role.getDsType();

		// 查询全部
		if (DataScopeTypeEnum.ALL.getType() == dsType) {
			return getTree();
		}
		// 自定义
		if (DataScopeTypeEnum.CUSTOM.getType() == dsType) {
			String dsScope = role.getDsScope();
			List<Integer> deptIds = Arrays.stream(dsScope.split(","))
					.map(Integer::parseInt).collect(Collectors.toList());
			List<Dept> depts = listByIds(deptIds).stream().collect(Collectors.toList());
			return getDeptTree(depts);
		}
		// 查询本级及其下级
		if (DataScopeTypeEnum.OWN_CHILD_LEVEL.getType() == dsType) {
			List<DeptTree> deptTreeList = new ArrayList<>();
			//本级
			Dept dept = super.getById(user.getDeptId());
			DeptTree deptTree = new DeptTree();
			deptTree.setId(dept.getId());
			deptTree.setParentId(dept.getParentId());
			deptTree.setName(dept.getName());
			//子级部门
			List<DeptTree> children = selectTree(user.getDeptId());
			deptTree.setChildren(children);
			deptTreeList.add(deptTree);
			return deptTreeList;
		}
		// 只查询本级
		if (DataScopeTypeEnum.OWN_LEVEL.getType() == dsType) {
			Dept dept = getById(user.getId());
			List<Dept> list = new ArrayList<>();
			list.add(dept);
			return getDeptTree(list);
		}

		return new ArrayList<>();
	}
}
