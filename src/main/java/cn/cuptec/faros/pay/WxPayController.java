package cn.cuptec.faros.pay;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.pay.PayConfig;

import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.service.*;
import cn.hutool.core.util.IdUtil;
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
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

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
    @Resource
    private UserServicePackageInfoService userServicePackageInfoService;
    @Resource
    private ServicePackageInfoService servicePackageInfoService;
    @Resource
    private DoctorTeamPeopleService doctorTeamPeopleService;
    @Resource
    private ChatUserService chatUserService;
    @Resource
    private RetrieveOrderService retrieveOrderService;
    @Resource
    private OrderRefundInfoService orderRefundInfoService;

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
        String SubMchId = rs.getSubMchId();
        WxPayConfig wxPayConfig = new WxPayConfig();
        wxPayConfig.setSubMchId(SubMchId);
        WxPayService wxPayService = PayConfig.getPayService(wxPayConfig);
        try {
            WxPayOrderNotifyResult notifyResult = wxPayService.parseOrderNotifyResult(xmlData);

            log.info("微信支付成回掉-=======" + notifyResult.toString());
            String transactionId = notifyResult.getTransactionId();
            String outTradeNo = notifyResult.getOutTradeNo();
            UserOrder userOrder = userOrdertService.getOne(new QueryWrapper<UserOrder>().lambda()
                    .eq(UserOrder::getOrderNo, outTradeNo));
            userOrder.setTransactionId(transactionId);
            userOrder.setStatus(2);//已支付 待发货
            userOrder.setPayTime(LocalDateTime.now());
            //为用户创建群聊
            Integer doctorTeamId = userOrder.getDoctorTeamId();
            List<DoctorTeamPeople> doctorTeamPeopleList = doctorTeamPeopleService.list(new QueryWrapper<DoctorTeamPeople>().lambda()
                    .eq(DoctorTeamPeople::getTeamId, doctorTeamId));
            if (!CollectionUtils.isEmpty(doctorTeamPeopleList)) {
                List<Integer> userIds = doctorTeamPeopleList.stream().map(DoctorTeamPeople::getUserId)
                        .collect(Collectors.toList());
                chatUserService.saveGroupChatUser(userIds, doctorTeamId);

            }
            userOrdertService.updateById(userOrder);
            //添加用户自己的服务
            Integer servicePackId = userOrder.getServicePackId();

            List<ServicePackageInfo> servicePackageInfos = servicePackageInfoService.list(new QueryWrapper<ServicePackageInfo>().lambda()
                    .eq(ServicePackageInfo::getServicePackageId, servicePackId));
            if (!CollectionUtils.isEmpty(servicePackageInfos)) {
                List<UserServicePackageInfo> userServicePackageInfos = new ArrayList<>();
                for (ServicePackageInfo servicePackageInfo : servicePackageInfos) {
                    UserServicePackageInfo userServicePackageInfo = new UserServicePackageInfo();
                    userServicePackageInfo.setUserId(userOrder.getUserId());
                    userServicePackageInfo.setOrderId(userOrder.getId());
                    userServicePackageInfo.setServicePackageInfoId(servicePackageInfo.getId());
                    userServicePackageInfo.setCreateTime(LocalDateTime.now());
                    userServicePackageInfos.add(userServicePackageInfo);
                }
                userServicePackageInfoService.saveBatch(userServicePackageInfos);
            }
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
     * @return 退款操作结果 wx pay refund result
     * @throws WxPayException the wx pay exception
     */
    @ApiOperation(value = "申请退款")
    @GetMapping("/refundOrder")
    public RestResponse refundOrder(@RequestParam("retrieveOrderId") Integer retrieveOrderId, @RequestParam("refundReason") String refundReson, @RequestParam("amount") Double amount) {
        RetrieveOrder retrieveOrder = retrieveOrderService.getById(retrieveOrderId);
        UserOrder userOrder = userOrdertService.getById(retrieveOrder.getOrderId());
        //添加退款记录
        OrderRefundInfo orderRefunds = new OrderRefundInfo();
        orderRefunds.setOrderId(retrieveOrder.getOrderId());
        orderRefunds.setRefundReason(refundReson);
        orderRefunds.setRefundFee(new BigDecimal(amount).multiply(new BigDecimal(100)));
        orderRefunds.setCreateId(SecurityUtils.getUser().getId());
        orderRefunds.setCreateTime(new Date());
        orderRefunds.setRefundStatus(1);
        orderRefunds.setRetrieveOrderId(retrieveOrderId);
        orderRefunds.setOrderRefundNo(IdUtil.getSnowflake(0, 0).nextIdStr());
        orderRefunds.setTransactionId(userOrder.getTransactionId());
        orderRefundInfoService.save(orderRefunds);


        Dept dept = deptService.getById(userOrder.getDeptId());

        WxPayRefundRequest request = new WxPayRefundRequest();
        request.setSubMchId(dept.getSubMchId());
        request.setTransactionId(userOrder.getTransactionId());
        request.setOutRefundNo(orderRefunds.getOrderRefundNo());
        request.setTotalFee(userOrder.getPayment().multiply(new BigDecimal(100)).intValue());
        request.setRefundFee(new BigDecimal(amount).multiply(new BigDecimal(100)).intValue());
        request.setNotifyUrl("https://pharos3.ewj100.com/wxpay/notifyRefunds");

        WxPayConfig wxPayConfig = new WxPayConfig();
        wxPayConfig.setAppId("wxad59cd874b45bb96");
        wxPayConfig.setSubMchId(request.getSubMchId());
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
            //需要测试一下
            WxPayRefundNotifyResult notifyResult = wxPayService.parseRefundNotifyResult(xmlData);
            String outTradeNo = notifyResult.getReqInfo().getOutTradeNo();
            OrderRefundInfo one = orderRefundInfoService.getOne(new QueryWrapper<OrderRefundInfo>().lambda()
                    .eq(OrderRefundInfo::getOrderRefundNo, outTradeNo));
            one.setSuccessTime(new Date());
            one.setRefundStatus(2);

            System.out.println(outTradeNo);
            return RestResponse.ok(notifyResult);
        } catch (WxPayException e) {
            e.printStackTrace();
            return RestResponse.failed(e.getErrCodeDes());
        }
    }

}


