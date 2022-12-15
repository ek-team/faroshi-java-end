package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.Dept;
import cn.cuptec.faros.entity.DeptRelation;
import cn.cuptec.faros.mapper.DeptRelationMapper;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 服务实现类
 */
@Service
@AllArgsConstructor
public class DeptRelationService extends ServiceImpl<DeptRelationMapper, DeptRelation> {

    private final DeptRelationMapper tenantDeptRelationMapper;

	/**
	 * 维护部门关系
	 *
	 * @param dept 部门
	 */
	@Transactional(rollbackFor = Exception.class)
	public void insertDeptRelation(Dept dept) {
		//增加部门关系表
		DeptRelation condition = new DeptRelation();
		condition.setDescendant(dept.getParentId());
		List<DeptRelation> relationList = tenantDeptRelationMapper
			.selectList(Wrappers.<DeptRelation>query().lambda()
				.eq(DeptRelation::getDescendant, dept.getParentId()))
			.stream().map(relation -> {
				relation.setDescendant(dept.getId());
				return relation;
			}).collect(Collectors.toList());
		if (CollUtil.isNotEmpty(relationList)) {
			this.saveBatch(relationList);
		}

		//自己也要维护到关系表中
		DeptRelation own = new DeptRelation();
		own.setDescendant(dept.getId());
		own.setAncestor(dept.getId());
		tenantDeptRelationMapper.insert(own);
	}

	/**
	 * 通过ID删除部门关系
	 *
	 * @param id
	 */
	public void deleteAllDeptRealtion(Integer id) {
		baseMapper.deleteDeptRelationsById(id);
	}

	/**
	 * 更新部门关系
	 *
	 * @param relation
	 */
	@Transactional
	public void updateDeptRealtion(DeptRelation relation) {
		baseMapper.updateDeptRelations(relation);
	}

}
