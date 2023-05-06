package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.common.constrants.QrCodeConstants;
import cn.cuptec.faros.common.utils.http.ServletUtils;
import cn.cuptec.faros.config.com.Url;
import cn.cuptec.faros.config.security.service.CustomUser;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.service.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.AllArgsConstructor;
import org.springframework.security.oauth2.provider.ClientDetails;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@AllArgsConstructor
@RestController
@RequestMapping("/test")
public class TestController {
    private final Url url;
    @Resource
    private ClientDetailsService clientDetailsService;
    @Resource
    private HospitalInfoService hospitalInfoService;
    @Resource
    private PlanUserService planUserService;
    @Resource
    private cn.cuptec.faros.service.WxMpService wxMpService;
    @Resource
    private RentRuleOrderService rentRuleOrderService;
    @Resource
    private UserOrdertService userOrdertService;
    @Resource
    private UserServicePackageInfoService userServicePackageInfoService;
    @GetMapping("user")
    public RestResponse customUserInfo() {

        RentRuleOrder rentRuleOrder = rentRuleOrderService.getOne(new QueryWrapper<RentRuleOrder>().lambda().eq(RentRuleOrder::getRentRuleOrderNo, "1654674697675079680"));
        if (rentRuleOrder != null) {
            if (rentRuleOrder.getStatus().equals(2)) {
                return RestResponse.ok();
            }
            rentRuleOrder.setStatus(2);
            rentRuleOrder.setTransactionId("4200001835202305066288764118");
            rentRuleOrderService.updateById(rentRuleOrder);
            UserOrder userOrderOne = userOrdertService.getOne(new QueryWrapper<UserOrder>().lambda().eq(UserOrder::getOrderNo, rentRuleOrder.getUserOrderNo()));
            List<UserServicePackageInfo> userServicePackageInfos = userServicePackageInfoService.list(new QueryWrapper<UserServicePackageInfo>().lambda().eq(UserServicePackageInfo::getOrderId, userOrderOne.getId()));
            if (!CollectionUtils.isEmpty(userServicePackageInfos)) {
                for (UserServicePackageInfo userServicePackageInfo : userServicePackageInfos) {
                    LocalDateTime expiredTime = userServicePackageInfo.getExpiredTime();
                    LocalDateTime now = LocalDateTime.now();
                    if (expiredTime.isAfter(now)) {
                        //没过期
                        Long aLong = Long.getLong(rentRuleOrder.getDay());

                        userServicePackageInfo.setExpiredTime(expiredTime.plusDays(aLong));
                    } else {
                        //过期
                        userServicePackageInfo.setExpiredTime(now.plusDays(Long.getLong(rentRuleOrder.getDay())));

                    }
                    if (rentRuleOrder.getServiceCount() != null) {
                        userServicePackageInfo.setTotalCount(userServicePackageInfo.getTotalCount() + rentRuleOrder.getServiceCount());
                    }
                }
                userServicePackageInfoService.updateBatchById(userServicePackageInfos);
            }


        }
//        List<HospitalInfo> list = hospitalInfoService.list();
//        for (HospitalInfo importDoctor : list) {
//            String hospitalInfoStr = importDoctor.getProvince() + importDoctor.getCity() + importDoctor.getArea() + importDoctor.getName();
//
//            importDoctor.setHospitalInfoStr(hospitalInfoStr);
//        }
//        hospitalInfoService.updateBatchById(list);

//        List<TbTrainUser> list = planUserService.list(new QueryWrapper<TbTrainUser>().lambda()
//
//                .isNull(TbTrainUser::getIdCard).or().eq(TbTrainUser::getIdCard, ""));
//        for (TbTrainUser tbTrainUser : list) {
//            tbTrainUser.setIdCard(tbTrainUser.getCaseHistoryNo());
//        }
//        planUserService.updateBatchById(list);
        return RestResponse.ok();
    }


}
