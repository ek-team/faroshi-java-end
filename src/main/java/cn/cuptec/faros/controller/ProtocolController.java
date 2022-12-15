package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.common.exception.InnerException;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.service.ProtocolService;
import cn.cuptec.faros.service.UserRoleService;
import cn.cuptec.faros.service.UserService;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/protocols")
public class ProtocolController extends AbstractBaseController<ProtocolService, Protocol> {
    @Resource
    private UserService userService;

    @GetMapping("/page")
    public RestResponse page() {
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());

        Page<Protocol> page = getPage();
        return RestResponse.ok(service.pageData(page, queryWrapper));
    }

    @GetMapping("getById/{id}")
    public RestResponse getById(@PathVariable int id) {
        return RestResponse.ok(service.getById(id));
    }

    @PutMapping("/updateById")
    public RestResponse updateById(@RequestBody Protocol protocol) {
        User byId = userService.getById(SecurityUtils.getUser().getId());
        if(!protocol.getDeptId().equals(byId.getDeptId()))
            throw  new InnerException("无权限修改，只能在同一个部门下");

        return RestResponse.ok(service.updateById(protocol));
    }

    @PostMapping("/save")
    public RestResponse save(@RequestBody Protocol protocol) {
        protocol.setDeptId(userService.getById(SecurityUtils.getUser().getId()).getDeptId());

        return RestResponse.ok(service.save(protocol));
    }

    @DeleteMapping("/del")
    public RestResponse del(@RequestBody Protocol protocol) {


        return RestResponse.ok(service.removeById(protocol.getId()));
    }

    @Override
    protected Class<Protocol> getEntityClass() {
        return Protocol.class;
    }
}
