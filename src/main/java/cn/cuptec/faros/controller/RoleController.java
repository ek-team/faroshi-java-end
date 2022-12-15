package cn.cuptec.faros.controller;

import cn.cuptec.faros.annotation.SysLog;
import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.Role;
import cn.cuptec.faros.service.RoleMenuService;
import cn.cuptec.faros.service.RoleService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/role")
public class RoleController extends AbstractBaseController<RoleService, Role> {

    @Resource
    private RoleMenuService roleMenuService;

    /**
     * 获取当前用户拥有的角色
     * @return
     */
    @GetMapping("/myRoles")
    public RestResponse listMyRole(){
        return RestResponse.ok(service.findRolesByUserId(SecurityUtils.getUser().getId()));
    }

    @GetMapping("/page")
    public RestResponse getRolePage() {
        Page<Role> page = getPage();
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());
        return RestResponse.ok(service.page(page, queryWrapper));
    }

    //获取角色列表
    @GetMapping("/list")
    public RestResponse listRoles() {
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());
        return RestResponse.ok(service.list(queryWrapper));
    }

    //获取角色列表及每个角色包含的用户数量
    @GetMapping("/list-cascade")
    public RestResponse listRoleAndUserCount() {
        List<Role> tenantRoles = service.listCascade(false);
        return RestResponse.ok(tenantRoles);
    }

    //todo 角色包含的用户列表、角色权限对应关系
    //通过ID查询角色信息")
    @GetMapping("/{id}")
	@PreAuthorize("@pms.hasPermission('role_detail')")
    public RestResponse<Role> getById(@PathVariable Integer id) {
        return RestResponse.ok(service.getById(id));
    }

    @SysLog("添加角色")
    @PostMapping
	@PreAuthorize("@pms.hasPermission('role_add')")
    public RestResponse save(@Valid @RequestBody Role role) {
        return RestResponse.ok(service.save(role));
    }

    @SysLog("修改角色")
    @PutMapping
	@PreAuthorize("@pms.hasPermission('role_edit')")
    public RestResponse update(@Valid @RequestBody Role role) {
        return service.updateById(role) ? RestResponse.ok(DATA_UPDATE_SUCCESS) : RestResponse.failed(DATA_UPDATE_FAILED);
    }

    @SysLog("删除角色")
    @DeleteMapping("/{id}")
	@PreAuthorize("@pms.hasPermission('role_del')")
    public RestResponse removeById(@PathVariable Integer id) {
        return RestResponse.ok(service.removeRoleById(id));
    }

    @SysLog("更新角色菜单")
    @PutMapping("/menu")
	@PreAuthorize("@pms.hasPermission('role_edit')")
    public RestResponse saveRoleMenus(@RequestParam Integer roleId, @RequestParam(value = "menuIds", required = false) List<Integer> menuIds) {
        return RestResponse.ok(roleMenuService.saveRoleMenus(roleId, menuIds));
    }

    @PostMapping("/initAdministratorMenu")
    public RestResponse initAdministratorMenu(){
        service.initAdministratorMenu();
        return RestResponse.ok();
    }

    @Override
    protected Class<Role> getEntityClass() {
        return Role.class;
    }
}
