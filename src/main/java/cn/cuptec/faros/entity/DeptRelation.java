package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.extension.activerecord.Model;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 部门关系表
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class DeptRelation extends Model<DeptRelation> {

	private static final long serialVersionUID = 1L;

	/**
	 * 祖先节点
	 */
	private Integer ancestor;
	/**
	 * 后代节点
	 */
	private Integer descendant;

}
