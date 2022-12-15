package cn.cuptec.faros.mapper;


import cn.cuptec.faros.entity.Role;
import cn.cuptec.faros.entity.UserRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户角色表 Mapper 接口
 */
public interface UserRoleMapper extends BaseMapper<UserRole> {
	/**
	 * 根据用户Id删除该用户的角色关系
	 *
	 * @param userId 用户ID
	 * @return boolean
	 */
	@Delete("DELETE FROM user_role WHERE user_id = #{userId}")
	Boolean deleteByUserId(@Param("userId") Integer userId);

	@Select("SELECT DISTINCT user_id from user_role")
    List<Integer> listDistinctUserHasRole();

}
