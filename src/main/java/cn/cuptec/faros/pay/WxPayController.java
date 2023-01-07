package cn.cuptec.faros.pay;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.pay.PayConfig;

import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.im.proto.ChatProto;
import cn.cuptec.faros.service.*;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import com.github.binarywang.wxpay.bean.notify.WxPayRefundNotifyResult;
import com.github.binarywang.wxpay.bean.request.WxPayRefundRequest;
import com.github.binarywang.wxpay.config.WxPayConfig;
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
    @Resource
    private WxPayFarosService wxPayFarosService;
    @Resource
    private UserFollowDoctorService userFollowDoctorService;
    @Resource
    private PatientOtherOrderService patientOtherOrderService;//患者其它订单
    @Resource
    private DoctorPointService doctorPointService;//医生积分
    @Resource
    private ChatMsgService chatMsgService;
    @Resource
    private DiseasesService diseasesService;
    /**
     * 调用统一下单接口，并组装生成支付所需参数对象.
     *
     * @return 返回 {@link com.github.binarywang.wxpay.bean.order}包下的类对象
     */
    @ApiOperation(value = "调用统一下单接口")
    @GetMapping("/unifiedOrder")
    public RestResponse unifiedOrder(@RequestParam("orderNo") String orderNo) {

        return wxPayFarosService.unifiedOrder(orderNo);

    }

    /**
     * 支付图文咨询申请
     */
    @GetMapping("/unifiedOtherOrder")
    public RestResponse unifiedOtherOrder(@RequestParam("orderNo") String orderNo) {

        return wxPayFarosService.unifiedOtherOrder(orderNo);

    }

    /**
     * 处理支付回调数据
     *
     * @return
     */
    @ApiOperation(value = "处理支付回调数据")
    @GetMapping("/notifyOrder")
    public RestResponse notifyOrder(@RequestParam("outTradeNo") String outTradeNo, @RequestParam("transactionId") String transactionId) {

        UserOrder userOrder = userOrdertService.getOne(new QueryWrapper<UserOrder>().lambda()
                .eq(UserOrder::getOrderNo, outTradeNo));
        if (userOrder != null) {


            if (userOrder.getStatus().equals(2)) {
                return RestResponse.ok();
            }
            userOrder.setConfirmPayTime(new Date());
            userOrder.setTransactionId(transactionId);
            userOrder.setStatus(2);//已支付 待发货
            userOrder.setPayTime(LocalDateTime.now());
            //为用户创建群聊
            Integer doctorTeamId = userOrder.getDoctorTeamId();
            List<DoctorTeamPeople> doctorTeamPeopleList = doctorTeamPeopleService.list(new QueryWrapper<DoctorTeamPeople>().lambda()
                    .eq(DoctorTeamPeople::getTeamId, doctorTeamId));
            ChatUser chatUser = new ChatUser();
            if (!CollectionUtils.isEmpty(doctorTeamPeopleList)) {
                List<UserFollowDoctor> userFollowDoctors = new ArrayList<>();
                List<Integer> userIds = doctorTeamPeopleList.stream().map(DoctorTeamPeople::getUserId)
                        .collect(Collectors.toList());
                for (Integer doctorId : userIds) {
                    UserFollowDoctor userDoctorRelation = new UserFollowDoctor();
                    userDoctorRelation.setDoctorId(doctorId);
                    userDoctorRelation.setUserId(userOrder.getUserId());
                    userFollowDoctors.add(userDoctorRelation);
                }
                userFollowDoctorService.remove(new QueryWrapper<UserFollowDoctor>().lambda()
                        .eq(UserFollowDoctor::getUserId, userOrder.getUserId())
                        .in(UserFollowDoctor::getDoctorId, userIds));
                //添加医生和患者的好友关系
                userFollowDoctorService.saveBatch(userFollowDoctors);
                userIds.add(userOrder.getUserId());
                chatUser = chatUserService.saveGroupChatUser(userIds, doctorTeamId, userOrder.getUserId());
            }
            //修改用户的病种
            Integer diseasesId = userOrder.getDiseasesId();
            if(diseasesId!=null){
                Diseases diseases = diseasesService.getById(diseasesId);
                if(diseases!=null){
                    User user=new User();
                    user.setId(userOrder.getUserId());
                    user.setDiseasesName(diseases.getName());
                    userService.updateById(user);
                }
            }
            userOrder.setChatUserId(chatUser.getId());
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
                    userServicePackageInfo.setTotalCount(servicePackageInfo.getCount());
                    userServicePackageInfo.setChatUserId(chatUser.getId());
                    userServicePackageInfo.setServicePackageInfoId(servicePackageInfo.getId());
                    userServicePackageInfo.setCreateTime(LocalDateTime.now());
                    userServicePackageInfo.setExpiredTime(LocalDateTime.now().plusDays(servicePackageInfo.getExpiredDay()));
                    userServicePackageInfos.add(userServicePackageInfo);
                }
                userServicePackageInfoService.saveBatch(userServicePackageInfos);
            }
        }
        //图文咨询订单处理
        PatientOtherOrder patientOtherOrder = patientOtherOrderService.getOne(new QueryWrapper<PatientOtherOrder>().lambda()
                .eq(PatientOtherOrder::getOrderNo, outTradeNo));
        if (patientOtherOrder != null) {
            if (patientOtherOrder.getStatus().equals(2)) {
                return RestResponse.ok();
            }
            patientOtherOrder.setStatus(2);
            patientOtherOrderService.updateById(patientOtherOrder);
            Integer chatUserId = patientOtherOrder.getChatUserId();
            ChatUser chatUser = chatUserService.getById(chatUserId);
            chatUser.setServiceStartTime(LocalDateTime.now());
            chatUser.setServiceEndTime(LocalDateTime.now().plusHours(patientOtherOrder.getHour()));
            chatUserService.updateById(chatUser);


            DoctorPoint doctorPoint = new DoctorPoint();
            doctorPoint.setPoint(patientOtherOrder.getAmount());
            doctorPoint.setDoctorTeamId(patientOtherOrder.getDoctorTeamId());
            doctorPoint.setDoctorUserId(patientOtherOrder.getDoctorId());
            doctorPoint.setPointDesc("图文咨询积分");
            doctorPoint.setWithdrawStatus(1);
            doctorPoint.setCreateTime(LocalDateTime.now());
            doctorPoint.setOrderNo(patientOtherOrder.getOrderNo());
            doctorPointService.save(doctorPoint);
        }

        return RestResponse.ok();
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


