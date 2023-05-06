package cn.cuptec.faros.controller;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.service.*;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RestController
@RequestMapping("/deviceScanSignLog")
public class DeviceScanSignLogController extends AbstractBaseController<DeviceScanSignLogService, DeviceScanSignLog> {

    @Resource
    private PlanUserService planUserService;
    @Resource
    private ProductStockService productStockService;
    @Resource
    private NaLiUserInfoService naLiUserInfoService;
    @Resource
    private UserService userService;

    @GetMapping("/list")
    public RestResponse list(@RequestParam("macAddress") String macAddress) {
        List<TbTrainUser> result = new ArrayList<>();

        List<DeviceScanSignLog> list = service.list(new QueryWrapper<DeviceScanSignLog>().lambda()
                .eq(DeviceScanSignLog::getMacAddress, macAddress));
        if (!CollectionUtils.isEmpty(list)) {
            List<String> userIds = list.stream().map(DeviceScanSignLog::getUserId)
                    .collect(Collectors.toList());
            List<TbTrainUser> list1 = planUserService.list(new QueryWrapper<TbTrainUser>().lambda()
                    .in(TbTrainUser::getUserId, userIds));
            if (!CollectionUtils.isEmpty(list1)) {
                list1.sort((t1, t2) -> t2.getCreateDate().compareTo(t1.getCreateDate()));
                result.add(list1.get(0));
                return RestResponse.ok(result);
            }

        }
        return RestResponse.ok(result);

//        List<TbTrainUser> tbTrainUsers = CollUtil.toList();
//        ProductStock one = productStockService.getOne(new QueryWrapper<ProductStock>().lambda().eq(ProductStock::getMacAddress, macAddress).eq(ProductStock::getDel, 1));
//        List<DeviceScanSignLog> list = service.list(new QueryWrapper<DeviceScanSignLog>().select("`user_id`,`create_time`,`id`").lambda().eq(DeviceScanSignLog::getMacAddress, macAddress).orderByDesc(DeviceScanSignLog::getCreateTime));
//        if (CollUtil.isNotEmpty(list)) {
//            List<String> names = new ArrayList<>();
//            List<DeviceScanSignLog> deviceScanSignLogs = list.stream().filter(// 过滤去重
//                    v -> {
//                        boolean flag = !names.contains(v.getUserId());
//                        names.add(v.getUserId());
//                        return flag;
//                    }
//            ).collect(Collectors.toList());
//            List<String> userIds = deviceScanSignLogs.stream().map(DeviceScanSignLog::getUserId).collect(Collectors.toList());
//            tbTrainUsers = planUserService.list(new QueryWrapper<TbTrainUser>().lambda().in(TbTrainUser::getXtUserId, userIds).orderByDesc(TbTrainUser::getUpdateDate));
//            //根据时间排序
//            Map<String, DeviceScanSignLog> map = deviceScanSignLogs.stream()
//                    .collect(Collectors.toMap(DeviceScanSignLog::getUserId, t -> t));
//            if (!CollectionUtils.isEmpty(tbTrainUsers)) {
//                for (TbTrainUser tbTrainUser : tbTrainUsers) {
//                    tbTrainUser.setCreateDate(map.get(tbTrainUser.getXtUserId()).getCreateTime());
//                }
//            }
//            tbTrainUsers.sort((t1, t2) -> t2.getCreateDate().compareTo(t1.getCreateDate()));
//        } else {
//            return RestResponse.ok(tbTrainUsers);
//        }
//        //判断设备是否绑定身份证
//        if (one != null) {
//            List<NaLiUserInfo> naLiUserInfos = naLiUserInfoService.list(new QueryWrapper<NaLiUserInfo>().lambda().eq(NaLiUserInfo::getProductSn, one.getProductSn()).orderByDesc(NaLiUserInfo::getCreateTime));
//            if (!CollectionUtils.isEmpty(naLiUserInfos)) {
//                NaLiUserInfo naLiUserInfo = naLiUserInfos.get(0);
//                //家庭版设备
//                //取出最后一个登陆用户
//
//                TbTrainUser tbTrainUser = tbTrainUsers.get(0);
//                if (StringUtils.isEmpty(tbTrainUser.getIdCard())) {
//                    //用户没绑定身份证
//                    TbTrainUser tbTrainUser1 = planUserService.getOne(new QueryWrapper<TbTrainUser>().lambda().eq(TbTrainUser::getIdCard, naLiUserInfo.getIdCard()));
//                    if (tbTrainUser1 != null) {
//                        tbTrainUser1.setIdCard("");
//                        planUserService.updateById(tbTrainUser1);
//                    }
//
//                    LambdaUpdateWrapper wrapper = new UpdateWrapper<TbTrainUser>().lambda()
//                            .set(TbTrainUser::getIdCard, naLiUserInfo.getIdCard())
//                            .eq(TbTrainUser::getUserId, tbTrainUsers.get(0).getUserId());
//                    planUserService.update(wrapper);
//                    List<TbTrainUser> result = new ArrayList<>();
//                    result.add(tbTrainUsers.get(0));
//                    return RestResponse.ok(result);
//                } else {
//
//                }
//                List<TbTrainUser> result = new ArrayList<>();
//                result.add(tbTrainUsers.get(0));
//                return RestResponse.ok(result);
//            } else {
//                //医用版设备
//                return RestResponse.ok(tbTrainUsers);
//            }
//        } else {
//            return RestResponse.ok(tbTrainUsers);
//        }

    }

    @PostMapping("/save")
    public RestResponse save(@RequestParam("macAddress") String macAddress, @RequestParam(value = "userId", required = false) String userId) {
        log.info("进入====================保存扫描设备:{}", macAddress);
        if (SecurityUtils.getUser() != null && StringUtils.isEmpty(userId)) {
            Integer id = SecurityUtils.getUser().getId();
            log.info("微信用户id" + id);
            if (id != null) {
                List<TbTrainUser> tbTrainUsers = planUserService.list(new QueryWrapper<TbTrainUser>().lambda().eq(TbTrainUser::getXtUserId, id));
                if (!CollectionUtils.isEmpty(tbTrainUsers)) {
                    TbTrainUser tbTrainUser = tbTrainUsers.get(0);
                    userId = tbTrainUser.getUserId();
                }
            }

        }


        log.info("保存扫描设备:{}", macAddress);
        log.info("保存扫描设备:{}", userId);
        service.remove(new QueryWrapper<DeviceScanSignLog>().lambda().eq(DeviceScanSignLog::getMacAddress, macAddress).eq(DeviceScanSignLog::getUserId, userId));
        DeviceScanSignLog deviceScanSignLog = new DeviceScanSignLog();

        deviceScanSignLog.setUserId(userId + "");

        deviceScanSignLog.setMacAddress(macAddress);


        return RestResponse.ok(service.save(deviceScanSignLog));

    }


    @DeleteMapping("/deleteByMacAddress/{macAddress}")
    public RestResponse deleteByMacAddress(@PathVariable String macAddress) {
        service.remove(new UpdateWrapper<DeviceScanSignLog>().lambda().eq(DeviceScanSignLog::getMacAddress, macAddress));
        return RestResponse.ok();
    }

    @GetMapping("/removeByMacAddress")
    public RestResponse removeByMacAddress(@RequestParam("macAddress") String macAddress) {
        service.remove(new UpdateWrapper<DeviceScanSignLog>().lambda().eq(DeviceScanSignLog::getMacAddress, macAddress));
        return RestResponse.ok();
    }

    @GetMapping("/add")
    public RestResponse add(@RequestParam("macAddress") String macAddress, @RequestParam(value = "userId", required = false) String userId) {
        service.remove(new QueryWrapper<DeviceScanSignLog>().lambda().eq(DeviceScanSignLog::getMacAddress, macAddress).eq(DeviceScanSignLog::getUserId, userId));
        DeviceScanSignLog deviceScanSignLog = new DeviceScanSignLog();

        deviceScanSignLog.setUserId(userId + "");

        deviceScanSignLog.setMacAddress(macAddress);


        return RestResponse.ok(service.save(deviceScanSignLog));

    }


    @GetMapping("/saveOther")
    public RestResponse saveOther(@RequestParam("macAddress") String macAddress, @RequestParam(value = "userId", required = false) String userId) {
        service.remove(new QueryWrapper<DeviceScanSignLog>().lambda().eq(DeviceScanSignLog::getMacAddress, macAddress).eq(DeviceScanSignLog::getUserId, userId));
        DeviceScanSignLog deviceScanSignLog = new DeviceScanSignLog();

        deviceScanSignLog.setUserId(userId + "");

        deviceScanSignLog.setMacAddress(macAddress);


        return RestResponse.ok(service.save(deviceScanSignLog));

    }

    @GetMapping("/getByMacAdd")
    public List<TbTrainUser> getByMacAdd(@RequestParam("macAddress") String macAddress) {

        List<DeviceScanSignLog> list = service.list(new QueryWrapper<DeviceScanSignLog>().lambda()
                .eq(DeviceScanSignLog::getMacAddress, macAddress));
        if (!CollectionUtils.isEmpty(list)) {
            List<String> userIds = list.stream().map(DeviceScanSignLog::getUserId)
                    .collect(Collectors.toList());
            List<TbTrainUser> list1 = planUserService.list(new QueryWrapper<TbTrainUser>().lambda()
                    .in(TbTrainUser::getUserId, userIds));
            if (CollectionUtils.isEmpty(list1)) {
                return new ArrayList<>();
            }
            list1.sort((t1, t2) -> t2.getCreateDate().compareTo(t1.getCreateDate()));
            List<TbTrainUser> result = new ArrayList<>();
            result.add(list1.get(0));
            return result;
        }
        return new ArrayList<>();

    }

    @GetMapping("/listData")
    public RestResponse listData() {
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());
        List<DeviceScanSignLog> deviceScanSignLogs = service.list(queryWrapper);
        if (!CollectionUtils.isEmpty(deviceScanSignLogs)) {
            List<String> userIds = deviceScanSignLogs.stream().map(DeviceScanSignLog::getUserId).collect(Collectors.toList());
            List<TbTrainUser> users = planUserService.list(new QueryWrapper<TbTrainUser>()
                    .lambda().in(TbTrainUser::getUserId, userIds));
            Map<String, TbTrainUser> map = users.stream()
                    .collect(Collectors.toMap(TbTrainUser::getUserId, t -> t));
            for (DeviceScanSignLog deviceScanSignLog : deviceScanSignLogs) {
                TbTrainUser user = map.get(deviceScanSignLog.getUserId());
                if (user != null) {
                    deviceScanSignLog.setUserName(user.getName());
                    deviceScanSignLog.setIdCard(user.getIdCard());
                    deviceScanSignLog.setPhone(user.getTelePhone());
                }
            }
        }
        return RestResponse.ok(deviceScanSignLogs);
    }

    @Override
    protected Class<DeviceScanSignLog> getEntityClass() {
        return DeviceScanSignLog.class;
    }
}