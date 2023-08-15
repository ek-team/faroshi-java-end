package cn.cuptec.faros.controller;

import cn.cuptec.faros.annotation.SysLog;
import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.Dept;
import cn.cuptec.faros.entity.User;
import cn.cuptec.faros.service.DeptService;
import cn.cuptec.faros.service.UserService;
import cn.cuptec.faros.vo.DeptTree;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 部门管理 前端控制器
 */
@RestController
@AllArgsConstructor
@RequestMapping("/dept")
public class DeptController extends AbstractBaseController<DeptService, Dept> {
    @Resource
    private UserService userService;

    //根据id查询部门
    @GetMapping("/{id}")
    public RestResponse<Dept> getById(@PathVariable Integer id) {
        return RestResponse.ok(service.getById(id));
    }

    //批量获取部门
    @GetMapping("/get-batch")
    public RestResponse<List<Dept>> getBatch(@RequestParam List<Integer> ids) {
        Assert.notEmpty(ids, "部门id集合不能为空");
        return RestResponse.ok(service.list(Wrappers.<Dept>lambdaQuery().in(Dept::getId, ids)));
    }

    @GetMapping("/list")
    public RestResponse<List<Dept>> list() {
        return RestResponse.ok(service.list());
    }

    //返回树形菜单集合
    @GetMapping(value = "/tree")
    public RestResponse<List<DeptTree>> getTree() {
        return RestResponse.ok(service.getTree());
    }

    @GetMapping("/treeScoped")
    public RestResponse treeScoped() {
        return RestResponse.ok(service.treeScoped());
    }

    //添加部门
    @SysLog("添加部门")
    @PostMapping
//	@PreAuthorize("@pms.hasPermission('sys_dept_add')")
    public RestResponse<Boolean> save(@Valid @RequestBody Dept dept) {
        return RestResponse.ok(service.saveDept(dept));
    }

    //删除部门
    @SysLog("删除部门")
    @DeleteMapping("/{id}")
//	@PreAuthorize("@pms.hasPermission('sys_dept_del')")
    public RestResponse<Boolean> removeById(@PathVariable Integer id) {
        return RestResponse.ok(service.removeDeptById(id));
    }

    //编辑部门
    @SysLog("编辑部门")
    @PutMapping
//	@PreAuthorize("@pms.hasPermission('sys_dept_edit')")
    public RestResponse update(@Valid @RequestBody Dept dept) {
        dept.setUpdateTime(LocalDateTime.now());
        return RestResponse.ok(service.updateDeptById(dept));
    }

    //批量编辑部门
    @SysLog("批量编辑部门")
    @PutMapping("batch-edit")
    public RestResponse updateBatch(@RequestBody List<Dept> depts) {
        return service.updateBatch(depts) ? RestResponse.ok() : RestResponse.failed();
    }

    //代理商设置联系电话
    @GetMapping("settingPhone")
    public RestResponse settingPhone(@RequestParam("phone") String phone) {
        User user = userService.getById(SecurityUtils.getUser().getId());
        Integer deptId = user.getDeptId();
        Dept dept = service.getById(deptId);
        dept.setPhone(phone);
        service.updateById(dept);
        return RestResponse.ok();
    }

    //查询代理商设置联系电话
    @GetMapping("getSettingPhone")
    public RestResponse getSettingPhone(@RequestParam(required = false, value = "deptId") Integer deptId) {
        if (deptId == null) {
            User user = userService.getById(SecurityUtils.getUser().getId());
            deptId = user.getDeptId();
        }
        Dept dept = service.getById(deptId);
        return RestResponse.ok(dept.getPhone());
    }

    @Override
    protected Class<Dept> getEntityClass() {
        return Dept.class;
    }

}
