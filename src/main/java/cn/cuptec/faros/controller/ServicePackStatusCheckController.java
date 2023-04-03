package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.ChatUser;
import cn.cuptec.faros.entity.ServicePack;
import cn.cuptec.faros.entity.ServicePackStatusCheck;
import cn.cuptec.faros.service.ServicePackService;
import cn.cuptec.faros.service.ServicePackStatusCheckService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 服务包审核
 */
@RestController
@RequestMapping("/servicePackStatusCheck")
public class ServicePackStatusCheckController extends AbstractBaseController<ServicePackStatusCheckService, ServicePackStatusCheck> {

    @Resource
    private ServicePackService servicePackService;

    /**
     * 停用服务包
     *
     * @param id
     * @return
     */
    @GetMapping("/stopServicePack")
    public RestResponse endedServicePack(@RequestParam("id") Integer id) {

        servicePackService.update(Wrappers.<ServicePack>lambdaUpdate()
                .eq(ServicePack::getId, id)
                .set(ServicePack::getStatus, 1)
        );
        ServicePackStatusCheck servicePackStatusCheck = new ServicePackStatusCheck();
        servicePackStatusCheck.setServicePackId(id);
        servicePackStatusCheck.setStatus(1);
        service.save(servicePackStatusCheck);
        return RestResponse.ok();


    }

    /**
     * 启动服务包
     *
     * @return
     */
    @GetMapping("/startServicePack")
    public RestResponse startServicePack(@RequestParam("servicePackId") Integer servicePackId) {

        servicePackService.update(Wrappers.<ServicePack>lambdaUpdate()
                .eq(ServicePack::getId, servicePackId)
                .set(ServicePack::getStatus, 0)
        );
        service.update(Wrappers.<ServicePackStatusCheck>lambdaUpdate()
                .eq(ServicePackStatusCheck::getServicePackId, servicePackId)
                .eq(ServicePackStatusCheck::getStatus, 1)
                .set(ServicePackStatusCheck::getStatus, 0)
        );
        return RestResponse.ok();


    }

    /**
     * 查询待启动服务包
     *
     * @return
     */
    @GetMapping("/quyeryStartServicePack")
    public RestResponse quyeryStartServicePack() {

        List<ServicePackStatusCheck> list = service.list(new QueryWrapper<ServicePackStatusCheck>().lambda().eq(ServicePackStatusCheck::getStatus, 1));
        if (!CollectionUtils.isEmpty(list)) {
            List<Integer> servicePackIds = list.stream().map(ServicePackStatusCheck::getServicePackId)
                    .collect(Collectors.toList());
            List<ServicePack> servicePacks = (List<ServicePack>) servicePackService.listByIds(servicePackIds);
            return RestResponse.ok(servicePacks);
        }
        return RestResponse.ok(new ArrayList<>());


    }

    @Override
    protected Class<ServicePackStatusCheck> getEntityClass() {
        return ServicePackStatusCheck.class;
    }
}
