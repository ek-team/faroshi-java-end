package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.service.HospitalInfoService;
import cn.cuptec.faros.service.OperationRecordService;
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
import java.util.*;
import java.util.stream.Collectors;

/**
 * 服务包审核
 */
@RestController
@RequestMapping("/servicePackStatusCheck")
public class ServicePackStatusCheckController extends AbstractBaseController<ServicePackStatusCheckService, ServicePackStatusCheck> {

    @Resource
    private ServicePackService servicePackService;
    @Resource
    private HospitalInfoService hospitalInfoService;
    @Resource
    private OperationRecordService operationRecordService;
    /**
     * 停用服务包
     *
     * @param id
     * @return
     */
    @GetMapping("/stopServicePack")
    public RestResponse endedServicePack(@RequestParam("id") Integer id) {
        //添加修改记录
        OperationRecord operationRecord = new OperationRecord();
        operationRecord.setStr(id + "");
        operationRecord.setType(3);
        operationRecord.setUserId(SecurityUtils.getUser().getId() + "");
        operationRecord.setCreateTime(new Date());
        operationRecord.setText("停用服务包");
        operationRecordService.save(operationRecord);

        servicePackService.update(Wrappers.<ServicePack>lambdaUpdate()
                .eq(ServicePack::getId, id)
                .set(ServicePack::getStatus, 1)
        );

        return RestResponse.ok();


    }

    /**
     * 启动服务包
     *
     * @return
     */
    @GetMapping("/startServicePack")
    public RestResponse startServicePack(@RequestParam("servicePackId") Integer servicePackId) {
        OperationRecord operationRecord = new OperationRecord();
        operationRecord.setStr(servicePackId + "");
        operationRecord.setType(3);
        operationRecord.setUserId(SecurityUtils.getUser().getId() + "");
        operationRecord.setCreateTime(new Date());
        operationRecord.setText("启用服务包");
        operationRecordService.save(operationRecord);
        servicePackService.update(Wrappers.<ServicePack>lambdaUpdate()
                .eq(ServicePack::getId, servicePackId)
                .set(ServicePack::getStatus, 2)
        );
        ServicePackStatusCheck servicePackStatusCheck = new ServicePackStatusCheck();
        servicePackStatusCheck.setServicePackId(servicePackId);
        servicePackStatusCheck.setStatus(2);
        service.save(servicePackStatusCheck);
        return RestResponse.ok();


    }

    /**
     * 审核服务包
     *
     * @return
     */
    @GetMapping("/verifyServicePack")
    public RestResponse verifyServicePack(@RequestParam("servicePackId") Integer servicePackId) {
        OperationRecord operationRecord = new OperationRecord();
        operationRecord.setStr(servicePackId + "");
        operationRecord.setType(3);
        operationRecord.setUserId(SecurityUtils.getUser().getId() + "");
        operationRecord.setCreateTime(new Date());
        operationRecord.setText("审核服务包");
        operationRecordService.save(operationRecord);

        servicePackService.update(Wrappers.<ServicePack>lambdaUpdate()
                .eq(ServicePack::getId, servicePackId)
                .set(ServicePack::getStatus, 0)
        );
        service.update(Wrappers.<ServicePackStatusCheck>lambdaUpdate()
                .eq(ServicePackStatusCheck::getServicePackId, servicePackId)
                .eq(ServicePackStatusCheck::getStatus, 2)
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

        List<ServicePackStatusCheck> list = service.list(new QueryWrapper<ServicePackStatusCheck>().lambda().eq(ServicePackStatusCheck::getStatus, 2));
        if (!CollectionUtils.isEmpty(list)) {
            List<Integer> servicePackIds = list.stream().map(ServicePackStatusCheck::getServicePackId)
                    .collect(Collectors.toList());
            List<ServicePack> servicePacks = (List<ServicePack>) servicePackService.listByIds(servicePackIds);
            List<Integer> hospitalIds = new ArrayList<>();
            for (ServicePack servicePack : servicePacks) {
                hospitalIds.add(servicePack.getHospitalId());
            }
            Map<Integer, HospitalInfo> hospitalInfoMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(hospitalIds)) {
                List<HospitalInfo> hospitalInfos = (List<HospitalInfo>) hospitalInfoService.listByIds(hospitalIds);
                hospitalInfoMap = hospitalInfos.stream()
                        .collect(Collectors.toMap(HospitalInfo::getId, t -> t));

            }

            for (ServicePack servicePack : servicePacks) {
                HospitalInfo hospitalInfo = hospitalInfoMap.get(servicePack.getHospitalId());
                if (hospitalInfo != null) {
                    servicePack.setHospitalName(hospitalInfo.getName());
                }
            }
            return RestResponse.ok(servicePacks);
        }
        return RestResponse.ok(new ArrayList<>());


    }

    @Override
    protected Class<ServicePackStatusCheck> getEntityClass() {
        return ServicePackStatusCheck.class;
    }
}
