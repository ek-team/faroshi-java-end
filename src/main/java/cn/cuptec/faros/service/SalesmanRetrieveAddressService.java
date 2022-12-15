package cn.cuptec.faros.service;

import cn.cuptec.faros.common.constrants.CommonConstants;
import cn.cuptec.faros.common.utils.StringUtils;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.entity.City;
import cn.cuptec.faros.entity.EvaluationRecords;
import cn.cuptec.faros.entity.SalesmanRetrieveAddress;
import cn.cuptec.faros.entity.User;
import cn.cuptec.faros.mapper.SalesmanRetrieveAddressMapper;
import com.baomidou.mybatisplus.core.toolkit.Assert;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
public class SalesmanRetrieveAddressService extends ServiceImpl<SalesmanRetrieveAddressMapper, SalesmanRetrieveAddress> {

    @Resource
    private CityService cityService;
    @Resource
    private UserService userService;

    @Override
    public boolean save(SalesmanRetrieveAddress entity) {

        Integer[] retrieveRegionIds = entity.getRetrieveRegions();
        if (retrieveRegionIds != null && retrieveRegionIds.length > 0) {
            List<String> regionNameList = new ArrayList<>();
            for (int i = 0; i < retrieveRegionIds.length; i++) {
                City city = cityService.getById(retrieveRegionIds[i]);
                regionNameList.add(city.getName());
            }
            entity.setRetrieveRegion(StringUtils.join(regionNameList, CommonConstants.VALUE_SEPARATOR));
        }
        entity.setSalesmanId(SecurityUtils.getUser().getId());
        User byId = userService.getById(SecurityUtils.getUser().getId());
        entity.setDeptId(byId.getDeptId());
        return super.save(entity);
    }

    @Override
    public boolean updateById(SalesmanRetrieveAddress entity) {
        Integer[] retrieveRegionIds = entity.getRetrieveRegions();
        if (retrieveRegionIds != null && retrieveRegionIds.length > 0) {
            List<String> regionNameList = new ArrayList<>();
            for (int i = 0; i < retrieveRegionIds.length; i++) {
                City city = cityService.getById(retrieveRegionIds[i]);
                regionNameList.add(city.getName());
            }
            entity.setRetrieveRegion(StringUtils.join(regionNameList, CommonConstants.VALUE_SEPARATOR));
        }
        entity.setSalesmanId(SecurityUtils.getUser().getId());
        return super.updateById(entity);
    }
}
