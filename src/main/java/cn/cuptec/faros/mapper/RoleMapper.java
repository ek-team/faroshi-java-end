package cn.cuptec.faros.mapper;

import cn.cuptec.faros.entity.Role;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * <p>
 * Mapper 接口
 * </p>
 */
public interface RoleMapper extends BaseMapper<Role> {
	/**
	 * 通过用户ID，查询角色信息
	 *
	 * @param userId
	 * @return
	 */
	@Select("SELECT restResponse.* FROM role restResponse, user_role ur WHERE restResponse.id = ur.role_id AND restResponse.del_flag = 0 and  ur.user_id = #{userId}")
	List<Role> listRolesByUserId(Integer userId);
}
