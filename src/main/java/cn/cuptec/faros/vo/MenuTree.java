package cn.cuptec.faros.vo;

import cn.cuptec.faros.common.bean.TreeNode;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class MenuTree extends TreeNode {
	private String permission;
	private String icon;
	private String name;
	private boolean spread = false;
	private String path;
	private String authority;
	private String redirect;
	private String keepAlive;
	private String code;
	private String type;
	private String label;
	private Integer sort;

	public MenuTree() {
	}

	public MenuTree(int id, String name, int parentId) {
		this.id = id;
		this.parentId = parentId;
		this.name = name;
		this.label = name;
	}

	public MenuTree(int id, String name, MenuTree parent) {
		this.id = id;
		this.parentId = parent.getId();
		this.name = name;
		this.label = name;
	}

	public MenuTree(MenuVO menuVo) {
		this.id = menuVo.getId();
		this.parentId = menuVo.getParentId();
		this.icon = menuVo.getIcon();
		this.permission = menuVo.getPermission();
		this.name = menuVo.getName();
		this.path = menuVo.getPath();
		this.type = menuVo.getType();
		this.label = menuVo.getName();
		this.sort = menuVo.getSort();
		this.keepAlive = menuVo.getKeepAlive();
	}
}
