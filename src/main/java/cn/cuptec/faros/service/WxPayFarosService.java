package cn.cuptec.faros.service;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.pay.PayResult;
import cn.cuptec.faros.pay.PayResultData;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;

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


    public RestResponse unifiedOrder(String orderNo) {
        UserOrder userOrder = userOrdertService.getOne(new QueryWrapper<UserOrder>().lambda().eq(UserOrder::getOrderNo, orderNo));
        if (!userOrder.getStatus().equals(1)) {
            return RestResponse.failed("订单已支付");
        }
        Dept dept = deptService.getById(userOrder.getDeptId());
        User user = userService.getById(SecurityUtils.getUser().getId());

        String url = "https://api.redadzukibeans.com/weChat/wxpay/otherUnifiedOrder?orderNo=" + orderNo + "&openId=" + user.getMaOpenId() + "&subMchId=" + dept.getSubMchId() + "&payment=" + userOrder.getPayment().multiply(new BigDecimal(100)).intValue();
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

        String url = "https://api.redadzukibeans.com/weChat/wxpay/otherUnifiedOrder?orderNo=" + orderNo + "&openId=" + user.getMaOpenId() + "&subMchId=" + dept.getSubMchId() + "&payment=" + new BigDecimal(patientOtherOrder.getAmount()).multiply(new BigDecimal(100)).intValue();
        String result = HttpUtil.get(url);
        PayResult wenXinInfo = JSONObject.parseObject(result, PayResult.class);
        PayResultData data = wenXinInfo.getData();
        data.setOrderId(patientOtherOrder.getId());
        return RestResponse.ok(data);
    }

    public static void main(String[] args) {
        String url = "https://api.redadzukibeans.com/weChat/wxpay/otherUnifiedOrder?orderNo=1610097301647261696&openId=oA1V45fmdi2v4xhPHfE8NCywNQpM&subMchId=1634891163&payment=1";
        String result = HttpUtil.get(url);
        PayResult wenXinInfo = JSONObject.parseObject(result, PayResult.class);
        System.out.println(result + "============");
        System.out.println(wenXinInfo.getData().toString() + "============");
    }
}
