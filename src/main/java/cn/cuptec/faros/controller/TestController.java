package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.common.constrants.QrCodeConstants;
import cn.cuptec.faros.common.utils.http.ServletUtils;
import cn.cuptec.faros.config.com.Url;
import cn.cuptec.faros.config.security.service.CustomUser;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.service.*;
import cn.cuptec.faros.vo.MapExpressTrackVo;
import cn.hutool.core.util.IdUtil;
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
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@AllArgsConstructor
@RestController
@RequestMapping("/test")
public class TestController {
    private final Url url;
    @Resource
    private ProductStockService productStockService;
    @Resource
    private HospitalInfoService hospitalInfoService;
    @Resource
    private PlanUserService planUserService;
    @Resource
    private cn.cuptec.faros.service.WxMpService wxMpService;
    @Resource
    private RentRuleOrderService rentRuleOrderService;
    @Resource
    private DeptService deptService;
    @Resource
    private UserOrdertService userOrdertService;
    @Resource
    private ExpressService expressService;
    @Resource
    private DoctorTeamService doctorTeamService;
    @Resource
    private RetrieveOrderService retrieveOrderService;

    @GetMapping("user")
    public RestResponse customUserInfo() {
        RetrieveOrder retrieveOrder = new RetrieveOrder();

        retrieveOrder.setUserOrderNo("1673636740302110720");
        retrieveOrder.setRentDay(47);
        retrieveOrder.setOrderId("2041");
        retrieveOrder.setUserId(3302);
        retrieveOrder.setCreateTime(new Date());
        retrieveOrder.setOrderNo(IdUtil.getSnowflake(0, 0).nextIdStr());
        retrieveOrder.setStatus(2);

        retrieveOrder.setDeliverySn("SF1406528853973");
        retrieveOrder.setDeliveryCompanyCode("shunfeng");
        retrieveOrderService.saveRetrieveOrder(retrieveOrder);


        UserOrder userOrder = new UserOrder();
        userOrder.setStatus(5);
        userOrder.setUseDay(47);
        userOrder.setId(2041);
        userOrder.setRecycleTime(LocalDateTime.now());


        userOrdertService.updateById(userOrder);
        return RestResponse.ok();
    }


}
