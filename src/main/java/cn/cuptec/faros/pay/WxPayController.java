package cn.cuptec.faros.pay;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.pay.PayConfig;

import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.entity.Dept;
import cn.cuptec.faros.entity.User;
import cn.cuptec.faros.entity.UserOrder;
import cn.cuptec.faros.service.DeptService;
import cn.cuptec.faros.service.UserOrdertService;
import cn.cuptec.faros.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import com.github.binarywang.wxpay.bean.notify.WxPayRefundNotifyResult;
import com.github.binarywang.wxpay.bean.request.WxPayRefundRequest;
import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.constant.WxPayConstants;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/wxpay")
@Api(value = "wxpay", tags = "微信支付")
public class WxPayController {
    @Resource
    private UserService userService;
    @Resource
    private UserOrdertService userOrdertService;
    @Resource
    private DeptService deptService;

    /**
     * 调用统一下单接口，并组装生成支付所需参数对象.
     *
     * @return 返回 {@link com.github.binarywang.wxpay.bean.order}包下的类对象
     */
    @ApiOperation(value = "调用统一下单接口")
    @GetMapping("/unifiedOrder")
    public RestResponse unifiedOrder(@RequestParam("orderNo") String orderNo) {
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
        request.setSubOpenid(user.getMpOpenId());
        request.setSubAppId("wxad59cd874b45bb96");

        WxPayConfig wxPayConfig = new WxPayConfig();
        wxPayConfig.setSubMchId(request.getSubMchId());
        WxPayService wxPayService = PayConfig.getPayService(wxPayConfig);
        try {
            request.setNotifyUrl("http://pharos.ewj100.com/wxpay/notifyOrder");
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

    /**
     * 处理支付回调数据
     *
     * @param xmlData
     * @return
     */
    @ApiOperation(value = "处理支付回调数据")
    @PostMapping("/notifyOrder")
    public RestResponse notifyOrder(@RequestBody String xmlData) {
        WxPayOrderNotifyResult rs = WxPayOrderNotifyResult.fromXML(xmlData);
//		String appId = rs.getAppid();
        String SubMchId = rs.getSubMchId();
        WxPayConfig wxPayConfig = new WxPayConfig();
//		wxPayConfig.setAppId(appId);
        wxPayConfig.setSubMchId(SubMchId);
        WxPayService wxPayService = PayConfig.getPayService(wxPayConfig);
        try {
            WxPayOrderNotifyResult notifyResult = wxPayService.parseOrderNotifyResult(xmlData);

            log.info("微信支付成回掉-=======" + notifyResult.toString());
            String transactionId = notifyResult.getTransactionId();
            return RestResponse.ok(notifyResult);
        } catch (WxPayException e) {
            e.printStackTrace();
            return RestResponse.failed(e.getErrCodeDes());
        }
    }

    /**
     * <pre>
     * 微信支付-申请退款.
     * 详见 https://pay.weixin.qq.com/wiki/doc/api/jsapi.php?chapter=9_4
     * 接口链接：https://api.mch.weixin.qq.com/secapi/pay/refund
     * </pre>
     *
     * @param request 请求对象
     * @return 退款操作结果 wx pay refund result
     * @throws WxPayException the wx pay exception
     */
    @ApiOperation(value = "申请退款")
    @PostMapping("/refundOrder")
    public RestResponse refundOrder(@RequestBody WxPayRefundRequest request) {
        WxPayConfig wxPayConfig = new WxPayConfig();
        wxPayConfig.setAppId(request.getAppid());
        wxPayConfig.setSubMchId(request.getSubMchId());
        request.setNotifyUrl("http://pharos.ewj100.com/wxpay/notifyRefunds");
        WxPayService wxPayService = PayConfig.getPayService(wxPayConfig);
        try {
            return RestResponse.ok(wxPayService.refund(request));
        } catch (WxPayException e) {
            e.printStackTrace();
            return RestResponse.failed(e.getCustomErrorMsg());
        }
    }

    /**
     * 处理退款回调数据
     *
     * @param xmlData
     * @return
     */
    @ApiOperation(value = "处理退款回调数据")
    @PostMapping("/notifyRefunds")
    public RestResponse notifyRefunds(@RequestBody String xmlData) {
        log.info("退款回调:" + xmlData);
        WxPayOrderNotifyResult rs = WxPayOrderNotifyResult.fromXML(xmlData);
        log.info("退款回调:" + rs.toString());
        String appId = rs.getAppid();
        String SubMchId = rs.getSubMchId();
        WxPayConfig wxPayConfig = new WxPayConfig();
        wxPayConfig.setAppId(appId);
        wxPayConfig.setSubMchId(SubMchId);
        WxPayService wxPayService = PayConfig.getPayService(wxPayConfig);
        try {
            WxPayRefundNotifyResult notifyResult = wxPayService.parseRefundNotifyResult(xmlData);
            String outTradeNo = notifyResult.getReqInfo().getOutTradeNo();
            System.out.println(outTradeNo);
            return RestResponse.ok(notifyResult);
        } catch (WxPayException e) {
            e.printStackTrace();
            return RestResponse.failed(e.getErrCodeDes());
        }
    }

}


