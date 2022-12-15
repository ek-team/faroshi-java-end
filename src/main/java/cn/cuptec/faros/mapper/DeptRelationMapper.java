package cn.cuptec.faros.mapper;

import cn.cuptec.faros.entity.DeptRelation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Update;

public interface DeptRelationMapper extends BaseMapper<DeptRelation> {
	/**
	 * 删除部门关系表数据
	 *
	 * @param id 部门ID
	 */
	@Delete("DELETE FROM dept_relation WHERE descendant IN " +
			"	(SELECT temp.descendant FROM " +
			"		(SELECT descendant FROM dept_relation WHERE ancestor = #{id}) temp" +
			"    )"
	)
	void deleteDeptRelationsById(Integer id);

	/**
	 * 更改部分关系表数据
	 *
	 * @param deptRelation
	 */
	@Update("DELETE FROM dept_relation WHERE descendant IN " +
			"( SELECT temp.descendant FROM ( SELECT descendant FROM dept_relation WHERE ancestor = #{descendant} ) temp ) " +
			"AND ancestor IN ( SELECT temp.ancestor FROM ( SELECT ancestor FROM dept_relation WHERE descendant = #{descendant} AND ancestor != descendant ) temp ); " +
			"INSERT INTO dept_relation (ancestor, descendant) " +
			"SELECT a.ancestor, b.descendant " +
			"FROM dept_relation a " +
			"CROSS JOIN dept_relation b " +
			"WHERE a.descendant = #{ancestor} " +
			"AND b.ancestor = #{descendant};")
	void updateDeptRelations(DeptRelation deptRelation);

}
