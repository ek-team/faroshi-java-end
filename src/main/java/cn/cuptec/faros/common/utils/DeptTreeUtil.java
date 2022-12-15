package cn.cuptec.faros.common.utils;

import cn.cuptec.faros.vo.DeptTree;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class DeptTreeUtil {

    public static List<DeptTree> build(List<DeptTree> treeNodes, Integer root) {

        List<DeptTree> trees = new ArrayList<>();

        for (DeptTree treeNode : treeNodes) {

            if (root.equals(treeNode.getParentId())) {
                trees.add(treeNode);
            }

            for (DeptTree it : treeNodes) {
                if (it.getParentId() == treeNode.getId()) {
                    if (treeNode.getChildren() == null) {
                        treeNode.setChildren(new ArrayList<>());
                    }
                    treeNode.add(it);
                }
            }
        }
        return trees;
    }

}
