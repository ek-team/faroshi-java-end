package cn.cuptec.faros.service;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.com.Url;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.pay.PayResult;
import cn.cuptec.faros.pay.PayResultData;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.math.BigDecimal;

@AllArgsConstructor
@Service
public class WxPayFarosService {
    @Resource
    private UserService userService;
    @Resource
    private UserOrdertService userOrdertService;
    @Resource
    private DeptService deptService;
    @Resource
    private PatientOtherOrderService patientOtherOrderService;//患者其它订单
    private final Url urlData;

    public RestResponse unifiedOrder(String orderNo, String tradeType) {
        User user = userService.getById(SecurityUtils.getUser().getId());

        String openId = user.getMaOpenId();

        if (StringUtils.isEmpty(tradeType)) {
            tradeType = "JSAPI";
        } else {
            tradeType = "MWEB";
            openId = user.getMpOpenId();
        }
        UserOrder userOrder = userOrdertService.getOne(new QueryWrapper<UserOrder>().lambda().eq(UserOrder::getOrderNo, orderNo));
        if (!userOrder.getStatus().equals(1)) {
            return RestResponse.failed("订单已支付");
        }
        Dept dept = deptService.getById(userOrder.getDeptId());

        String url = urlData.getPayUrl() + "?orderNo=" + orderNo + "&openId=" + openId + "&subMchId=" + dept.getSubMchId() + "&payment=" + userOrder.getPayment().multiply(new BigDecimal(100)).intValue() + "&tradeType=" + tradeType;

        //String url = "https://api.redadzukibeans.com/weChat/wxpay/otherUnifiedOrder?orderNo=" + orderNo + "&openId=" + user.getMaOpenId() + "&subMchId=" + dept.getSubMchId() + "&payment=" + userOrder.getPayment().multiply(new BigDecimal(100)).intValue()+ "&tradeType=" + tradeType;
        String result = HttpUtil.get(url);
        PayResult wenXinInfo = JSONObject.parseObject(result, PayResult.class);
        return RestResponse.ok(wenXinInfo.getData());
    }

    public RestResponse unifiedOrder(Integer deptId, String orderNo, String tradeType, BigDecimal amount) {
        if (StringUtils.isEmpty(tradeType)) {
            tradeType = "JSAPI";
        } else {
            tradeType = "MWEB";
        }
        User user = userService.getById(SecurityUtils.getUser().getId());

        Dept dept = deptService.getById(deptId);
        String url = urlData.getPayUrl() + "?orderNo=" + orderNo + "&openId=" + user.getMaOpenId() + "&subMchId=" + dept.getSubMchId() + "&payment=" + amount.multiply(new BigDecimal(100)).intValue() + "&tradeType=" + tradeType;

        //String url = "https://api.redadzukibeans.com/weChat/wxpay/otherUnifiedOrder?orderNo=" + orderNo + "&openId=" + user.getMaOpenId() + "&subMchId=" + dept.getSubMchId() + "&payment=" + userOrder.getPayment().multiply(new BigDecimal(100)).intValue()+ "&tradeType=" + tradeType;
        String result = HttpUtil.get(url);
        PayResult wenXinInfo = JSONObject.parseObject(result, PayResult.class);
        return RestResponse.ok(wenXinInfo.getData());
    }

    public RestResponse unifiedOtherOrder(String orderNo) {
        PatientOtherOrder patientOtherOrder = patientOtherOrderService.getOne(new QueryWrapper<PatientOtherOrder>().lambda()
                .eq(PatientOtherOrder::getOrderNo, orderNo));
        if (!patientOtherOrder.getStatus().equals(1)) {
            return RestResponse.failed("订单已支付");
        }
        Dept dept = deptService.getById(patientOtherOrder.getDeptId());
        User user = userService.getById(SecurityUtils.getUser().getId());
        String url = urlData.getPayUrl() + "?orderNo=" + orderNo + "&openId=" + user.getMaOpenId() + "&subMchId=" + dept.getSubMchId() + "&payment=" + new BigDecimal(patientOtherOrder.getAmount() + "").multiply(new BigDecimal(100)).intValue() + "&tradeType=JSAPI";

        //String url = "https://api.redadzukibeans.com/weChat/wxpay/otherUnifiedOrder?orderNo=" + orderNo + "&openId=" + user.getMaOpenId() + "&subMchId=" + dept.getSubMchId() + "&payment=" + new BigDecimal(patientOtherOrder.getAmount()).multiply(new BigDecimal(100)).intValue()+ "&tradeType=JSAPI";
        String result = HttpUtil.get(url);
        PayResult wenXinInfo = JSONObject.parseObject(result, PayResult.class);
        PayResultData data = wenXinInfo.getData();
        data.setOrderId(patientOtherOrder.getId());
        return RestResponse.ok(data);
    }

}
