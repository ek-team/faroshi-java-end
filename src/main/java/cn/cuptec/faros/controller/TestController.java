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

    @GetMapping("user")
    public RestResponse customUserInfo() {
        wxMpService.paySuccessNoticeSalesman("oV8W46Jr8-9S-8aDSQ4Mcigwbwms", "您的客户已成功下单，请您尽快处理！",
                "下肢智能负重SHRJggj患者：陈1月兰订单号:1686546626912452608医院:上海交通大学医学院附属瑞金医院医生团队:王蕾主任团队", "9500.0",
                "点击查看详情", "https://api.jhxiao-school.com/index.html#/salesman/orderDetailster?id=11111");
        return RestResponse.ok();
    }


}
