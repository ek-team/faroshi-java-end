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
    private DeptService deptService;
    @Resource
    private UserOrdertService userOrdertService;
    @Resource
    private ExpressService expressService;
    @GetMapping("user")
    public RestResponse customUserInfo() {
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        List<UserOrder> list = userOrdertService.list(new QueryWrapper<UserOrder>().lambda()
                .ge(UserOrder::getStatus, 3)
                .ge(UserOrder::getPayment,10)
                .isNotNull(UserOrder::getDeliverySn)
                .isNull(UserOrder::getLogisticsDeliveryTime)
        );
        if (!CollectionUtils.isEmpty(list)) {
            for (UserOrder userOrder : list) {
                MapExpressTrackVo userOrderMapTrace = expressService.getUserOrderMapTraceNoMessage(userOrder.getId());
                if(userOrderMapTrace!=null){
                    MapExpressTrackVo.ExpressData[] data = userOrderMapTrace.getData();
                    MapExpressTrackVo.ExpressData datum = data[data.length - 1];
                    String time = datum.getTime();
                    LocalDateTime ldt = LocalDateTime.parse(time, df);
                    userOrder.setLogisticsDeliveryTime(ldt);
                    Integer state = userOrderMapTrace.getState();
//                if (state.equals(3)) {
//                    userOrder.setStatus(4);
//                }
                }

            }
            userOrdertService.updateBatchById(list);
        }


        return RestResponse.ok();
    }


}
