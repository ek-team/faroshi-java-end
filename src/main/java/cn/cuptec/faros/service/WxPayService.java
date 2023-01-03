package cn.cuptec.faros.service;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.pay.PayConfig;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.entity.Dept;
import cn.cuptec.faros.entity.User;
import cn.cuptec.faros.entity.UserOrder;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.exception.WxPayException;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.math.BigDecimal;

@Service
public class WxPayService {
    @Resource
    private UserService userService;
    @Resource
    private UserOrdertService userOrdertService;
    @Resource
    private DeptService deptService;


    public RestResponse unifiedOrder(String orderNo) {
        UserOrder userOrder = userOrdertService.getOne(new QueryWrapper<UserOrder>().lambda().eq(UserOrder::getOrderNo, orderNo));
        if (!userOrder.getStatus().equals(1)) {
            return RestResponse.failed("订单已支付");
        }
        Dept dept = deptService.getById(userOrder.getDeptId());
        User user = userService.getById(SecurityUtils.getUser().getId());
        WxPayUnifiedOrderRequest request = new WxPayUnifiedOrderRequest();
        request.setSubMchId(dept.getSubMchId());

        String body = "订单支付";
        request.setBody(body);
        request.setOutTradeNo(userOrder.getOrderNo());
        request.setTotalFee(userOrder.getPayment().multiply(new BigDecimal(100)).intValue());
        request.setTradeType(WxPayConstants.TradeType.JSAPI);
        request.setSpbillCreateIp("127.0.0.1");
        request.setSubOpenid(user.getMaOpenId());
        request.setSubAppId("wxad59cd874b45bb96");

        WxPayConfig wxPayConfig = new WxPayConfig();
        wxPayConfig.setSubMchId(request.getSubMchId());
        com.github.binarywang.wxpay.service.WxPayService wxPayService = PayConfig.getPayService(wxPayConfig);
        try {
            request.setNotifyUrl("https://pharos3.ewj100.com/wxpay/notifyOrder");
            request.setProductId(request.getOutTradeNo());

            return RestResponse.ok(wxPayService.createOrder(request));
        } catch (WxPayException e) {
            if ("INVALID_REQUEST".equals(e.getErrCode())) {
                return RestResponse.failed("订单号重复，请重新下单");
            }
            e.printStackTrace();
            return RestResponse.failed(e.getReturnMsg() + "" + e.getCustomErrorMsg() + "" + e.getErrCodeDes());
        }
    }

}
