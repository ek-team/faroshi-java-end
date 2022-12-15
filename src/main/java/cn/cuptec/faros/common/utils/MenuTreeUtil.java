package cn.cuptec.faros.common.utils;


import cn.cuptec.faros.entity.Menu;
import cn.cuptec.faros.vo.MenuTree;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class MenuTreeUtil extends TreeUtil {

	/**
	 * 通过sysMenu创建树形节点
	 *
	 * @param menus
	 * @param root
	 * @return
	 */
	public List<MenuTree> buildTree(List<Menu> menus, int root) {
		List<MenuTree> trees = new ArrayList<>();
		MenuTree node;
		for (Menu menu : menus) {
			node = new MenuTree();
			node.setId(menu.getId());
			node.setParentId(menu.getParentId());
			node.setName(menu.getName());
			node.setPath(menu.getPath());
			node.setCode(menu.getPermission());
			node.setType(menu.getType());
			node.setLabel(menu.getName());
			node.setIcon(menu.getIcon());
			node.setKeepAlive(menu.getKeepAlive());
			trees.add(node);
		}
		return MenuTreeUtil.build(trees, root);
	}


}
