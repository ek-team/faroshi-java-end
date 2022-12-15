package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.common.utils.StringUtils;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.DeviceVersion;
import cn.cuptec.faros.entity.ProductStock;
import cn.cuptec.faros.service.DeviceVersionService;
import cn.cuptec.faros.service.ProductStockService;
import cn.hutool.core.util.BooleanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/deviceVersion")
public class DeviceVersionController extends AbstractBaseController<DeviceVersionService, DeviceVersion> {
    @Resource
    private ProductStockService productStockService;

    @GetMapping("/page")
    public RestResponse page() {

        Page<DeviceVersion> page = getPage();
        QueryWrapper<DeviceVersion> queryWrapper = getQueryWrapper(getEntityClass());

        return RestResponse.ok(service.page(page, queryWrapper));
    }

    @GetMapping("/newVersion")
    public RestResponse newVersion(@RequestParam("type") Integer type, @RequestParam(value = "macAdd",required = false) String macAdd) {
        if (!StringUtils.isEmpty(macAdd)) {
            List<ProductStock> list = productStockService.list(new QueryWrapper<ProductStock>().lambda().eq(ProductStock::getMacAddress, macAdd)
                    .eq(ProductStock::getDel, 1));
            if (!CollectionUtils.isEmpty(list)) {
                ProductStock productStock = list.get(0);
                DeviceVersion deviceVersion = service.newVersion(productStock.getProductId());
                if (deviceVersion == null) {
                    deviceVersion = new DeviceVersion();
                }
                return RestResponse.ok(deviceVersion);
            }

        }
        DeviceVersion deviceVersion = service.newVersion(type);
        if (deviceVersion == null) {
            deviceVersion = new DeviceVersion();
        }
        return RestResponse.ok(deviceVersion);


    }


    @PostMapping("/save")
    @PreAuthorize("@pms.hasPermission('sys_device_version_save')")
    public RestResponse save(@RequestBody DeviceVersion deviceVersion) {
        deviceVersion.setUserId(SecurityUtils.getUser().getId());
        return RestResponse.ok(service.save(deviceVersion));
    }

    @PutMapping("/updateById")
    @PreAuthorize("@pms.hasPermission('sys_device_version_update')")
    public RestResponse updateById(@RequestBody DeviceVersion deviceVersion) {
        deviceVersion.setUserId(SecurityUtils.getUser().getId());
        return RestResponse.ok(service.updateById(deviceVersion));
    }

    @DeleteMapping("/deleteById")
    @PreAuthorize("@pms.hasPermission('sys_device_version_del')")
    public RestResponse delById(@RequestParam("id") int id) {
        return RestResponse.ok(service.removeById(id) ? RestResponse.ok(DATA_DELETE_SUCCESS) : RestResponse.failed(DATA_DELETE_FAILED));
    }

    @GetMapping("/listByCategory")
    public RestResponse listByCategory() {

        QueryWrapper<DeviceVersion> queryWrapper = getQueryWrapper(getEntityClass());

        return RestResponse.ok(service.list(queryWrapper));
    }


    @GetMapping("/listByType")
    public RestResponse listByType() {

        QueryWrapper<DeviceVersion> queryWrapper = getQueryWrapper(getEntityClass());
        queryWrapper.orderByAsc("type");
        return RestResponse.ok(service.list(queryWrapper));
    }

    @Override
    protected Class<DeviceVersion> getEntityClass() {
        return DeviceVersion.class;
    }
}