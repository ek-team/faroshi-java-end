package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.ProductStock;
import cn.cuptec.faros.entity.ServicePackageInfo;
import cn.cuptec.faros.entity.UserQrCode;
import cn.cuptec.faros.entity.UserServicePackageInfo;
import cn.cuptec.faros.service.ServicePackageInfoService;
import cn.cuptec.faros.service.UserServicePackageInfoService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户自己的服务信息
 */
@RestController
@RequestMapping("/userServicePackageInfo")
public class UserServicePackageInfoController extends AbstractBaseController<UserServicePackageInfoService, UserServicePackageInfo> {
    @Resource
    private ServicePackageInfoService servicePackageInfoService;

    /**
     * 查询用户自己的服务信息
     *
     * @return
     */
    @GetMapping("/listByUserId")
    public RestResponse listByUserId() {
        List<UserServicePackageInfo> list = service.list(new QueryWrapper<UserServicePackageInfo>().lambda()
                .eq(UserServicePackageInfo::getUserId, SecurityUtils.getUser().getId()));
        if (!CollectionUtils.isEmpty(list)) {
            List<Integer> ids = list.stream().map(UserServicePackageInfo::getServicePackageInfoId)
                    .collect(Collectors.toList());
            List<ServicePackageInfo> servicePackageInfos = (List<ServicePackageInfo>) servicePackageInfoService.listByIds(ids);
            Map<Integer, ServicePackageInfo> map = servicePackageInfos.stream()
                    .collect(Collectors.toMap(ServicePackageInfo::getId, t -> t));
            for (UserServicePackageInfo servicePackageInfo : list) {
                servicePackageInfo.setServicePackageInfo(map.get(servicePackageInfo.getServicePackageInfoId()));
            }
        }
        return RestResponse.ok(list);
    }


    @Override
    protected Class<UserServicePackageInfo> getEntityClass() {
        return UserServicePackageInfo.class;
    }
}
