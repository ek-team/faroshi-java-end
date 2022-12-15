package cn.cuptec.faros.vo;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * 部门树
 */
@Data
//public class DeptTree extends TreeNode {
public class DeptTree {

	private String name;

	private int id;

	private int parentId;


	private Integer userCount;

	protected List<DeptTree> children = new ArrayList<DeptTree>();

	public void add(DeptTree node) {
		children.add(node);
	}

}
