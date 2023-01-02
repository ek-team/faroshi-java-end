package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.SalesmanRetrieveAddress;
import cn.cuptec.faros.entity.User;
import cn.cuptec.faros.service.SalesmanRetrieveAddressService;
import cn.cuptec.faros.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 回收地址管理
 */
@RestController
@RequestMapping("/retriAddress")
public class SalesmanRetrieveAddressController extends AbstractBaseController<SalesmanRetrieveAddressService, SalesmanRetrieveAddress> {
    @Resource
    private UserService userService;

    @PostMapping("/save")
    public RestResponse save(@Valid @RequestBody SalesmanRetrieveAddress salesmanRetrieveAddress) {
        service.save(salesmanRetrieveAddress);
        return RestResponse.ok();
    }

    @PutMapping("/put")
    public RestResponse update(@Valid @RequestBody SalesmanRetrieveAddress salesmanRetrieveAddress) {
        service.updateById(salesmanRetrieveAddress);
        return RestResponse.ok();
    }

    @GetMapping("/deleteById")
    public RestResponse deleteById(@RequestParam("id") int id) {
        service.removeById(id);
        return RestResponse.ok();
    }

    @GetMapping("/getMy")
    public RestResponse getMy() {
        SalesmanRetrieveAddress one = service.getOne(Wrappers.<SalesmanRetrieveAddress>lambdaQuery().eq(SalesmanRetrieveAddress::getSalesmanId, SecurityUtils.getUser().getId()));
        return RestResponse.ok(one);
    }

    /**
     * 查询部门下的回收地址
     *
     * @return
     */
    @GetMapping("/list")
    public RestResponse list() {
        User byId = userService.getById(SecurityUtils.getUser().getId());
        QueryWrapper<SalesmanRetrieveAddress> queryWrapper = getQueryWrapper(getEntityClass());
        queryWrapper.eq("salesman_retrieve_address.dept_id", byId.getDeptId());
        List<SalesmanRetrieveAddress> list = service.list(queryWrapper);
        return RestResponse.ok(list);
    }

    /**
     * 查询部门下的默认回收地址
     *
     * @return
     */
    @GetMapping("/listByDeptId")
    public RestResponse listByDeptId(@RequestParam("deptId") Integer deptId) {
        QueryWrapper<SalesmanRetrieveAddress> queryWrapper = getQueryWrapper(getEntityClass());
        queryWrapper.eq("salesman_retrieve_address.dept_id", deptId);
        queryWrapper.eq("salesman_retrieve_address.default_status", 1);
        List<SalesmanRetrieveAddress> list = service.list(queryWrapper);
        if (CollectionUtils.isEmpty(list)) {
            return RestResponse.ok();
        }
        return RestResponse.ok(list.get(0));
    }

    @Override
    protected Class<SalesmanRetrieveAddress> getEntityClass() {
        return SalesmanRetrieveAddress.class;
    }
}
