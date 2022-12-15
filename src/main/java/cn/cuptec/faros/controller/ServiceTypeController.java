package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.service.ServiceTypeService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/servicetype")
public class ServiceTypeController extends AbstractBaseController<ServiceTypeService, ServiceType> {

    /**
     * 添加
     *
     * @return
     */
    @PostMapping("/add")
    public RestResponse save(@RequestBody ServiceType serviceType) {
        ServiceType one = service.getOne(new QueryWrapper<ServiceType>().lambda().eq(ServiceType::getServiceId, serviceType.getServiceId()));

        if (one != null) {
            return RestResponse.failed("已经有该服务id请修改");
        }
        return RestResponse.ok(service.save(serviceType));
    }

    /**
     * 分页查询
     *
     * @return
     */
    @GetMapping("/page")
    public RestResponse page() {
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());

        Page<ServiceType> page = getPage();
        return RestResponse.ok(service.page(page, queryWrapper));
    }

    /**
     * 删除
     *
     * @return
     */
    @GetMapping("/delete")
    public RestResponse deleteById(@RequestParam("id") String id) {
        service.removeById(id);
        return RestResponse.ok();
    }

    /**
     * 编辑
     *
     * @return
     */
    @PostMapping("updateById")
    public RestResponse updateById(@RequestBody ServiceType serviceType) {
        service.updateById(serviceType);
        return RestResponse.ok();
    }

    @Override
    protected Class<ServiceType> getEntityClass() {
        return ServiceType.class;
    }
}
