package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.dto.KuaiDiCallBackResult;
import cn.cuptec.faros.dto.ShunfnegcallbackData;
import cn.cuptec.faros.dto.ShunfnegcallbackParam;
import cn.cuptec.faros.dto.WaybillRoute;
import cn.cuptec.faros.entity.DeliveryInfo;
import cn.cuptec.faros.entity.SFMsgDataResult;
import cn.cuptec.faros.entity.UserOrder;
import cn.cuptec.faros.service.DeliveryInfoService;
import cn.cuptec.faros.service.ShunFengService;
import cn.cuptec.faros.service.UserOrdertService;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.domain.ZhimaCreditPeUserOrderSyncModel;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sun.org.apache.regexp.internal.RE;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/shunFeng")
public class ShunFengController {

    @Resource
    private UserOrdertService userOrdertService;
    @Resource
    private DeliveryInfoService deliveryInfoService;
    @Resource
    private ShunFengService shunFengService;

    /**
     * 路由推送接口
     *
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping("shunfnegcallback")
    public ShunfnegcallbackData kuaidicallback(HttpServletRequest request, @RequestBody String WaybillRoute) throws Exception {
        log.info("顺丰回调接口");

        log.info(WaybillRoute);
        ShunfnegcallbackParam data = JSONObject.parseObject(WaybillRoute, ShunfnegcallbackParam.class);
        String orderid = data.getBody().getWaybillRoute().get(0).getOrderid();
        UserOrder userOrder = userOrdertService.getOne(new QueryWrapper<UserOrder>().lambda()
                .eq(UserOrder::getOrderNo, orderid));
        List<WaybillRoute> waybillRoute = data.getBody().getWaybillRoute();
        if (!CollectionUtils.isEmpty(waybillRoute)) {
            List<DeliveryInfo> deliveryInfos = deliveryInfoService.list(new QueryWrapper<DeliveryInfo>().lambda()
                    .eq(DeliveryInfo::getUserOrderNo, orderid));
            if (!CollectionUtils.isEmpty(deliveryInfos)) {
                for (DeliveryInfo deliveryInfo : deliveryInfos) {
                    deliveryInfo.setStatus(waybillRoute.get(waybillRoute.size() - 1).getOpCode());
                }
                deliveryInfoService.updateBatchById(deliveryInfos);
            }
            if (waybillRoute.get(waybillRoute.size() - 1).getOpCode().equals("50")) {
                userOrder.setStatus(3);
                userOrder.setLogisticsDeliveryTime(LocalDateTime.now());

            }
            if (waybillRoute.get(waybillRoute.size() - 1).getOpCode().equals("80")) {
                userOrder.setStatus(4);

            }
            userOrdertService.updateById(userOrder);
        }


        ShunfnegcallbackData shunfnegcallbackData = new ShunfnegcallbackData();
        shunfnegcallbackData.setReturn_msg("成功");
        shunfnegcallbackData.setReturn_code("0000");
        return shunfnegcallbackData;
    }

    /**
     * 获取顺丰面单接口
     */
    @GetMapping("SFMiandan")
    public RestResponse SFMiandan(@RequestParam("waybillNo") String waybillNo) {

        return RestResponse.ok(shunFengService.SFMiandan(waybillNo));
    }
}
