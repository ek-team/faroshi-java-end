package cn.cuptec.faros.controller;

import cn.cuptec.faros.annotation.SysLog;
import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.common.bean.TreeNode;
import cn.cuptec.faros.common.constrants.CommonConstants;
import cn.cuptec.faros.common.utils.MenuTreeUtil;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.Menu;
import cn.cuptec.faros.service.MenuService;
import cn.cuptec.faros.vo.MenuTree;
import cn.cuptec.faros.vo.MenuVO;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/menu")
public class MenuController extends AbstractBaseController<MenuService, Menu> {

    //获取当前用户的树形菜单集合
    @GetMapping
    public RestResponse<List<? extends TreeNode>> getUserMenu() {
        // 获取符合条件的菜单
        Set<MenuVO> all = new HashSet<>();
        SecurityUtils.getRoles()
                .forEach(roleId -> all.addAll(service.findMenuByRoleId(roleId)));
        List<MenuTree> menuTreeList = all.stream()
                .filter(menuVo -> CommonConstants.MENU.equals(menuVo.getType()))
                .map(MenuTree::new)
                .sorted(Comparator.comparingInt(MenuTree::getSort))
                .collect(Collectors.toList());
        return RestResponse.ok(MenuTreeUtil.build(menuTreeList, CommonConstants.TREE_ROOT_ID));
    }

    //获取所有菜单的树形结构
    @PreAuthorize("@pms.hasPermission('menu_manage')")
    @GetMapping(value = "/tree")
    public RestResponse<List<? extends TreeNode>> getTree() {
        return RestResponse.ok(MenuTreeUtil.buildTree(service
                .list(Wrappers.<Menu>lambdaQuery()
                        .orderByAsc(Menu::getSort)), CommonConstants.TREE_ROOT_ID));
    }

    //根据角色id获取该角色拥有的菜单id集合
    @GetMapping("/tree/{roleId}")
    public RestResponse<List<Integer>> getRoleTree(@PathVariable Integer roleId) {
        List<Integer> collect = service.findMenuByRoleId(roleId)
                .stream()
                .map(MenuVO::getId)
                .collect(Collectors.toList());
        return RestResponse.ok(collect);
    }

    //通过ID查询菜单的详细信息
    @GetMapping("/{id}")
    public RestResponse getById(@PathVariable Integer id) {
        return RestResponse.ok(service.getById(id));
    }

    @SysLog("新增菜单")
    @PostMapping
    @PreAuthorize("@pms.hasPermission('menu_add')")
    public RestResponse<Boolean> save(@Valid @RequestBody Menu menu) {
        if (menu.getParentId() != null && menu.getParentId() != CommonConstants.TREE_ROOT_ID.intValue()){

        }
        return RestResponse.ok(service.save(menu));
    }

    @SysLog("删除菜单")
    @DeleteMapping("/{id}")
    @PreAuthorize("@pms.hasPermission('menu_del')")
    public RestResponse<Boolean> removeById(@PathVariable Integer id) {
        return service.removeMenuById(id);
    }

    @SysLog("更新菜单")
    @PutMapping
    @PreAuthorize("@pms.hasPermission('menu_edit')")
    public RestResponse update(@Valid @RequestBody Menu menu) {
        return RestResponse.ok(service.updateMenuById(menu));
    }

    @Override
    protected Class<Menu> getEntityClass() {
        return Menu.class;
    }
}
