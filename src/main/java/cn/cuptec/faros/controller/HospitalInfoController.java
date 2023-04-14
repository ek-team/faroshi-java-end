package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.common.constrants.CommonConstants;
import cn.cuptec.faros.common.utils.StringUtils;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.City;
import cn.cuptec.faros.entity.HospitalInfo;
import cn.cuptec.faros.entity.Locator;
import cn.cuptec.faros.service.CityService;
import cn.cuptec.faros.service.HospitalInfoService;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 医院管理
 */
@RestController
@RequestMapping("/HospitalInfo")
public class HospitalInfoController extends AbstractBaseController<HospitalInfoService, HospitalInfo> {
    @Resource
    private CityService cityService;

    @GetMapping("/page")
    public RestResponse page() {
        QueryWrapper<HospitalInfo> queryWrapper = getQueryWrapper(getEntityClass());

        Page<HospitalInfo> page = getPage();
        IPage<HospitalInfo> page1 = service.page(page, queryWrapper);
        List<HospitalInfo> records = page1.getRecords();
        if (CollectionUtils.isEmpty(records)) {
            return RestResponse.ok(page1);
        }
        for (HospitalInfo hospitalInfo : records) {
            hospitalInfo.setProvince(hospitalInfo.getProvince() + "/" + hospitalInfo.getCity() + "/" + hospitalInfo.getArea());
        }
        page1.setRecords(records);
        return RestResponse.ok(page1);
    }



    @GetMapping("/getDoctorByUserId")
    public RestResponse getDoctorInfoByUserId(@RequestParam("userId")  Integer userId) {


        return RestResponse.ok(service.getDoctorInfoByUserId(userId));
    }

    @GetMapping("/listByDoctor")
    public RestResponse listByDoctor(@RequestParam("id") Integer id) {


        return RestResponse.ok(service.listByDoctor(id));
    }

    @GetMapping("/list")
    public RestResponse list() {
        QueryWrapper<HospitalInfo> queryWrapper = getQueryWrapper(getEntityClass());

        return RestResponse.ok(service.list(queryWrapper));
    }

    @GetMapping("/listCompatible")
    public RestResponse listCompatible(HospitalInfo hospitalInfo) {

        return list();
    }



    @GetMapping("getById/{id}")
    public RestResponse getById(@PathVariable int id) {


        return RestResponse.ok(service.getById(id));
    }

    @PutMapping("/updateById")
    public RestResponse updateById(@RequestBody HospitalInfo hospitalInfo) {
        hospitalInfo = setRegion(hospitalInfo);
        return RestResponse.ok(service.updateById(hospitalInfo));
    }

    @PostMapping("/save")
    public RestResponse save(@RequestBody HospitalInfo hospitalInfo) {
        hospitalInfo = setRegion(hospitalInfo);
        String hospitalInfoStr = hospitalInfo.getProvince() + hospitalInfo.getCity() + hospitalInfo.getArea() + hospitalInfo.getName();

        hospitalInfo.setHospitalInfoStr(hospitalInfoStr);
        return RestResponse.ok(service.save(hospitalInfo));
    }

    private HospitalInfo setRegion(HospitalInfo hospitalInfo) {
        Integer[] locatorRegionIds = hospitalInfo.getLocatorRegions();
        if (locatorRegionIds != null && locatorRegionIds.length > 0) {
            String province = cityService.getById(locatorRegionIds[1]).getName();
            String city = cityService.getById(locatorRegionIds[2]).getName();
            String area = cityService.getById(locatorRegionIds[3]).getName();
            hospitalInfo.setProvince(province);
            hospitalInfo.setCity(city);
            hospitalInfo.setArea(area);
            hospitalInfo.setLocatorRegionIds(Arrays.stream(locatorRegionIds).map(Object::toString).collect(Collectors.joining(";")));
        }
        return hospitalInfo;
    }


    @DeleteMapping("/deleteById/{id}")
    public RestResponse delById(@PathVariable int id) {
        return RestResponse.ok(service.removeById(id) ? RestResponse.ok(DATA_DELETE_SUCCESS) : RestResponse.failed(DATA_DELETE_FAILED));
    }

    @Override
    protected Class<HospitalInfo> getEntityClass() {
        return HospitalInfo.class;
    }

    public static void main(String[] args) {

    }
}