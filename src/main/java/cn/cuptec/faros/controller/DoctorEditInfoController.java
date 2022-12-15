package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.service.DoctorEditInfoService;
import cn.cuptec.faros.service.HospitalInfoService;
import cn.cuptec.faros.service.LiveQrCodeService;
import cn.cuptec.faros.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Author mby
 * @Date 2021/8/17 14:48
 */
@RestController
@RequestMapping("/doctorEditInfo")
public class DoctorEditInfoController extends AbstractBaseController<DoctorEditInfoService, DoctorEditInfo> {

    @Resource
    private LiveQrCodeService liveQrCodeService;
    @Resource
    private HospitalInfoService hospitalInfoService;

    @Resource
    private UserService userService;

    /**
     * 添加
     *
     * @return
     */
    @PostMapping("/add")
    public RestResponse save(@Valid @RequestBody DoctorEditInfo doctorEditInfo) {
        LiveQrCode liveQrCode = new LiveQrCode();
        liveQrCode.setType(4);
        liveQrCodeService.add(liveQrCode);
        doctorEditInfo.setLiveQrCodeId(liveQrCode.getId());
        doctorEditInfo.setCreatId(SecurityUtils.getUser().getId());
        doctorEditInfo.setCreateTime(LocalDateTime.now());
        doctorEditInfo.setUpdateTime(LocalDateTime.now());
        User byId = userService.getById(SecurityUtils.getUser().getId());
        doctorEditInfo.setDeptId(byId==null?null:byId.getDeptId());
        return RestResponse.ok(service.save(doctorEditInfo));
    }

    /**
     * 分页查询
     *
     * @return
     */
    @GetMapping("/page")
    public RestResponse page() {
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());
        queryWrapper.eq("doctor_edit_info.creat_id", SecurityUtils.getUser().getId());
        queryWrapper.select().orderByDesc("doctor_edit_info.create_time");
        Page<DoctorEditInfo> page = getPage();
        IPage page1 = service.page(page, queryWrapper);
        List<DoctorEditInfo> records = page1.getRecords();
        if (!CollectionUtils.isEmpty(records)) {
            //查询医院名字
            List<Integer> hospitalIds = records.stream().map(DoctorEditInfo::getHospitalId)
                    .collect(Collectors.toList());
            List<HospitalInfo> hospitalInfos = (List<HospitalInfo>) hospitalInfoService.listByIds(hospitalIds);
            Map<Integer, HospitalInfo> hospitalInfoMap = hospitalInfos.stream()
                    .collect(Collectors.toMap(HospitalInfo::getId, t -> t));
            for (DoctorEditInfo doctorEditInfo : records) {
                if(hospitalInfoMap.get(doctorEditInfo.getHospitalId())!=null){
                    doctorEditInfo.setHospital(hospitalInfoMap.get(doctorEditInfo.getHospitalId()).getName());

                }
            }
            page1.setRecords(records);
        }
        return RestResponse.ok(page1);
    }

    /**
     * 删除二维码
     *
     * @return
     */
    @DeleteMapping("/{id}")
    public RestResponse deleteById(@PathVariable String id) {
        DoctorEditInfo doctorEditInfo = service.getById(id);
        service.removeById(id);
        liveQrCodeService.delete(doctorEditInfo.getLiveQrCodeId());
        return RestResponse.ok();
    }

    /**
     * 编辑二维码
     *
     * @return
     */
    @PostMapping("updateById")
    public RestResponse updateById(@RequestBody DoctorEditInfo doctorEditInfo) {
        User byId = userService.getById(SecurityUtils.getUser().getId());
        doctorEditInfo.setDeptId(byId==null?null:byId.getDeptId());
        service.updateById(doctorEditInfo);
        return RestResponse.ok();
    }

    @GetMapping("getById")
    public RestResponse getById(@RequestParam("id") String id) {
        DoctorEditInfo doctorEditInfo = service.getOne(Wrappers.<DoctorEditInfo>lambdaQuery().eq(DoctorEditInfo::getLiveQrCodeId, id));
        return RestResponse.ok(doctorEditInfo);
    }

    @Override
    protected Class<DoctorEditInfo> getEntityClass() {
        return DoctorEditInfo.class;
    }
}
