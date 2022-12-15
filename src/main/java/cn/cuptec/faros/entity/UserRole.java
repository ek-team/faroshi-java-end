package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 租户-用户角色表
 */
@Data
public class
UserRole {

	private static final long serialVersionUID = 1L;

	/**
	 * 用户ID
	 */
	@TableId(value = "user_id", type = IdType.INPUT)
	private Integer userId;
	/**
	 * 角色ID  1-技术员，2-业务员，3-医生，4-专家，5-围产科
	 */
	@TableId(value = "role_id", type = IdType.INPUT)
	private Integer roleId;

}
