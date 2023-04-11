package cn.cuptec.faros.pay;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.pay.PayConfig;

import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.im.proto.ChatProto;
import cn.cuptec.faros.service.*;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
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
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private UserDoctorRelationService userDoctorRelationService;
    @Resource
    private PatientOtherOrderService patientOtherOrderService;//患者其它订单
    @Resource
    private DoctorPointService doctorPointService;//医生积分
    @Resource
    private DoctorTeamService doctorTeamService;
    @Resource
    private DiseasesService diseasesService;
    @Resource
    private cn.cuptec.faros.service.WxMpService wxMpService;
    @Resource
    private PatientUserService patientUserService;
    @Resource
    private PatientRelationTeamService patientRelationTeamService;
    @Resource
    private ServicePackService servicePackService;
    @Resource
    private UserQrCodeService userQrCodeService;
    @Resource
    private UserOrderNotifyService userOrderNotifyService;
    @Resource
    private RentRuleOrderService rentRuleOrderService;

    /**
     * 调用统一下单接口，并组装生成支付所需参数对象.
     *
     * @return 返回 {@link com.github.binarywang.wxpay.bean.order}包下的类对象
     */
    @ApiOperation(value = "调用统一下单接口")
    @GetMapping("/unifiedOrder")
    public RestResponse unifiedOrder(@RequestParam("orderNo") String orderNo, @RequestParam(value = "tradeType", required = false) String tradeType) {
        String[] split = orderNo.split("-");
        orderNo = split[1];
        return wxPayFarosService.unifiedOrder(orderNo, tradeType);

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

            if (!userOrder.getStatus().equals(1)) {
                return RestResponse.ok();
            }
            User userById = userService.getById(userOrder.getUserId());
            //发送公众号通知
            wxMpService.paySuccessNotice(userById.getMpOpenId(), "您的订单已支付成功!请耐心等待发货", userOrder.getOrderNo(), userOrder.getPayment().toString(),
                    "点击查看详情", "pages/myOrder/myOrder");

            //发送公众号通知业务员
            LambdaQueryWrapper<UserQrCode> eq = new QueryWrapper<UserQrCode>().lambda()
                    .eq(UserQrCode::getQrCodeId, userOrder.getServicePackId());
            List<UserQrCode> userQrCodeList = userQrCodeService.list(
                    eq);
            List<String> userIdList = new ArrayList<>();
            List<UserOrderNotify> userOrderNotifyList = userOrderNotifyService.list();
            if (!CollectionUtils.isEmpty(userOrderNotifyList)) {
                userIdList = userOrderNotifyList.stream().map(UserOrderNotify::getUserId)
                        .collect(Collectors.toList());
            }

            if (!CollectionUtils.isEmpty(userQrCodeList)) {
                List<String> userIdLIst = userQrCodeList.stream().map(UserQrCode::getUserId)
                        .collect(Collectors.toList());
                userIdList.addAll(userIdLIst);
            }
            if (!CollectionUtils.isEmpty(userIdList)) {
                ServicePack servicePack = servicePackService.getById(userOrder.getServicePackId());
                String keyword1 = "";
                if (servicePack != null) {
                    keyword1 = servicePack.getName() + "患者：" + userById.getPatientName() + "订单号:" + outTradeNo;
                } else {
                    keyword1 = "患者：" + userById.getPatientName() + "订单号:" + outTradeNo;
                }
                List<User> clerkUser = (List<User>) userService.listByIds(userIdList);
                for (User user : clerkUser) {
                    if (clerkUser != null) {
                        if (!StringUtils.isEmpty(user.getMpOpenId())) {
                            wxMpService.paySuccessNotice(user.getMpOpenId(), "您的客户已成功下单，请您尽快处理！", keyword1, 1 + "",
                                    "请不要点击该消息，只作为通知", "pages/myOrder/myOrder");
                        }

                    }
                }

            }

            Integer patientUserId = userOrder.getPatientUserId();
            PatientUser byId = patientUserService.getById(patientUserId);
            if (byId != null) {
                userById.setPatientName(byId.getName());
                userById.setPatientId(byId.getId());
                userById.setIdCard(byId.getIdCard());
                userService.updateById(userById);
            }
            userOrder.setConfirmPayTime(new Date());
            userOrder.setTransactionId(transactionId);
            userOrder.setStatus(2);//已支付 待发货
            userOrder.setPayTime(LocalDateTime.now());
            //为用户创建群聊
            Integer doctorTeamId = userOrder.getDoctorTeamId();
            List<DoctorTeamPeople> doctorTeamPeopleList = doctorTeamPeopleService.list(new QueryWrapper<DoctorTeamPeople>().lambda()
                    .eq(DoctorTeamPeople::getTeamId, doctorTeamId));

            List<Integer> userIds = doctorTeamPeopleList.stream().map(DoctorTeamPeople::getUserId)
                    .collect(Collectors.toList());

            List<UserFollowDoctor> one = userFollowDoctorService.list(new QueryWrapper<UserFollowDoctor>().lambda()
                    .eq(UserFollowDoctor::getUserId, userOrder.getUserId())
                    .in(UserFollowDoctor::getTeamId, doctorTeamId));
            if (CollectionUtils.isEmpty(one)) {
                //添加医生团队的好友关系
                UserFollowDoctor userFollowDoctor = new UserFollowDoctor();
                userFollowDoctor.setTeamId(doctorTeamId);
                userFollowDoctor.setUserId(userOrder.getUserId());
                userFollowDoctorService.save(userFollowDoctor);
            }
            //患者和团队的关系
            patientRelationTeamService.remove(new QueryWrapper<PatientRelationTeam>().lambda()
                    .eq(PatientRelationTeam::getPatientId, userOrder.getUserId())
                    .eq(PatientRelationTeam::getTeamId, doctorTeamId));
            PatientRelationTeam patientRelationTeam = new PatientRelationTeam();

            patientRelationTeam.setPatientId(userOrder.getUserId());
            patientRelationTeam.setTeamId(doctorTeamId);
            patientRelationTeamService.save(patientRelationTeam);
            //添加医生和患者的关系
            List<UserDoctorRelation> userDoctorRelationList = new ArrayList<>();
            userDoctorRelationService.remove(new QueryWrapper<UserDoctorRelation>().lambda()
                    .eq(UserDoctorRelation::getUserId, userOrder.getUserId())
                    .in(UserDoctorRelation::getDoctorId, userIds));


            for (Integer doctorId : userIds) {
                UserDoctorRelation userDoctorRelation = new UserDoctorRelation();
                userDoctorRelation.setDoctorId(doctorId);
                userDoctorRelation.setUserId(userOrder.getUserId());
                userDoctorRelationList.add(userDoctorRelation);
            }
            userDoctorRelationService.saveBatch(userDoctorRelationList);

            userIds.add(userOrder.getUserId());
            ChatUser chatUser = chatUserService.saveGroupChatUser(userIds, doctorTeamId, userOrder.getUserId());

            //修改用户的病种
            Integer diseasesId = userOrder.getDiseasesId();
            if (diseasesId != null) {
                Diseases diseases = diseasesService.getById(diseasesId);
                if (diseases != null) {
                    User user = new User();
                    user.setId(userOrder.getUserId());
                    user.setDiseasesName(diseases.getName());
                    userService.updateById(user);
                }
            }
            userOrder.setChatUserId(chatUser.getId());

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
                userOrder.setUserServicePackageInfoId(userServicePackageInfos.get(0).getId());
            }

            userOrdertService.updateById(userOrder);
        }
        //图文咨询订单处理
        PatientOtherOrder patientOtherOrder = patientOtherOrderService.getOne(new QueryWrapper<PatientOtherOrder>().lambda()
                .eq(PatientOtherOrder::getOrderNo, outTradeNo));
        if (patientOtherOrder != null) {
            if (patientOtherOrder.getStatus().equals(2)) {
                return RestResponse.ok();
            }
            patientOtherOrder.setTransactionId(transactionId);
            patientOtherOrder.setStatus(2);
            patientOtherOrderService.updateById(patientOtherOrder);

            //判断是 个人咨询还是团队咨询
            if (patientOtherOrder.getDoctorId() != null) {
                DoctorPoint doctorPoint = new DoctorPoint();
                doctorPoint.setPoint(patientOtherOrder.getAmount());
                doctorPoint.setDoctorUserId(patientOtherOrder.getDoctorId());
                doctorPoint.setPointDesc("图文咨询");
                doctorPoint.setWithdrawStatus(1);
                doctorPoint.setPatientId(patientOtherOrder.getUserId());
                doctorPoint.setCreateTime(LocalDateTime.now());
                doctorPoint.setOrderNo(patientOtherOrder.getOrderNo());
                doctorPointService.save(doctorPoint);
            } else {
                //团队咨询
                //判断是是抢单模式还是 非抢单模式
                Integer doctorTeamId = patientOtherOrder.getDoctorTeamId();
                DoctorTeam doctorTeam = doctorTeamService.getById(doctorTeamId);
                if (doctorTeam.getModel().equals(2)) {
                    //非抢单模式
                    DoctorPoint doctorPoint = new DoctorPoint();
                    doctorPoint.setPoint(patientOtherOrder.getAmount());
                    doctorPoint.setDoctorTeamId(patientOtherOrder.getDoctorTeamId());
                    doctorPoint.setLeaderId(doctorTeam.getLeaderId());
                    doctorPoint.setDoctorUserId(doctorTeam.getLeaderId());
                    doctorPoint.setPointDesc("图文咨询");
                    doctorPoint.setWithdrawStatus(1);
                    doctorPoint.setPatientId(patientOtherOrder.getUserId());
                    doctorPoint.setCreateTime(LocalDateTime.now());
                    doctorPoint.setOrderNo(patientOtherOrder.getOrderNo());
                    doctorPointService.save(doctorPoint);

                }
            }


            ChatUser chatUser = chatUserService.getById(patientOtherOrder.getChatUserId());
            if (chatUser.getGroupType().equals(0)) {
                ChatUser fromUserChat = new ChatUser();
                fromUserChat.setUid(chatUser.getUid());
                fromUserChat.setTargetUid(chatUser.getTargetUid());


                ChatUser toUserChat = new ChatUser();
                toUserChat.setUid(chatUser.getTargetUid());
                toUserChat.setTargetUid(chatUser.getUid());

                List<ChatUser> chatUsers = new ArrayList<>();
                chatUsers.add(fromUserChat);
                chatUsers.add(toUserChat);

                chatUsers.forEach(c -> {
                    ChatUser one = chatUserService.getOne(Wrappers.<ChatUser>lambdaQuery().eq(ChatUser::getTargetUid, c.getTargetUid()).eq(ChatUser::getUid, c.getUid()));
                    if (one != null) {

                        chatUserService.update(Wrappers.<ChatUser>lambdaUpdate()
                                .eq(ChatUser::getUid, c.getUid())
                                .eq(ChatUser::getTargetUid, c.getTargetUid())
                                .set(ChatUser::getChatDesc, "咨询")
                                .set(ChatUser::getPatientOtherOrderStatus, "0")
                                .set(ChatUser::getChatCount, 9)
                                .set(ChatUser::getPatientOtherOrderNo, patientOtherOrder.getId() + "")

                        );
                    }
                });
            } else {
                chatUser.setChatDesc("咨询");
                chatUser.setChatCount(9);
                chatUser.setPatientOtherOrderStatus("0");
                chatUser.setPatientOtherOrderNo(patientOtherOrder.getId() + "");
                chatUserService.updateById(chatUser);
            }

        }


        // 续租订单
        RentRuleOrder rentRuleOrder = rentRuleOrderService.getOne(new QueryWrapper<RentRuleOrder>().lambda().eq(RentRuleOrder::getRentRuleOrderNo, outTradeNo));
        if (rentRuleOrder != null) {
            if (!rentRuleOrder.getStatus().equals(2)) {
                return RestResponse.ok();
            }
            rentRuleOrder.setStatus(2);
            rentRuleOrder.setTransactionId(transactionId);
            rentRuleOrderService.updateById(rentRuleOrder);
            UserOrder userOrderOne = userOrdertService.getOne(new QueryWrapper<UserOrder>().lambda().eq(UserOrder::getOrderNo, rentRuleOrder.getUserOrderNo()));
            List<UserServicePackageInfo> userServicePackageInfos = userServicePackageInfoService.list(new QueryWrapper<UserServicePackageInfo>().lambda().eq(UserServicePackageInfo::getOrderId, userOrderOne.getId()));
            if (!CollectionUtils.isEmpty(userServicePackageInfos)) {
                for (UserServicePackageInfo userServicePackageInfo : userServicePackageInfos) {
                    LocalDateTime expiredTime = userServicePackageInfo.getExpiredTime();
                    LocalDateTime now = LocalDateTime.now();
                    if (expiredTime.isAfter(now)) {
                        //没过期
                        userServicePackageInfo.setExpiredTime(expiredTime.plusDays(Long.getLong(rentRuleOrder.getDay())));
                    } else {
                        //过期
                        userServicePackageInfo.setExpiredTime(now.plusDays(Long.getLong(rentRuleOrder.getDay())));

                    }
                }
                userServicePackageInfoService.updateBatchById(userServicePackageInfos);
            }


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
    public RestResponse refundOrder(@RequestParam("retrieveOrderId") Integer retrieveOrderId, @RequestParam(value = "refundReason", required = false) String refundReson, @RequestParam("amount") Double amount) {

        RetrieveOrder retrieveOrder = retrieveOrderService.getById(retrieveOrderId);
        Integer status = retrieveOrder.getStatus();
        if (!status.equals(3)) {
            return RestResponse.ok();
        }

        UserOrder userOrder = userOrdertService.getById(retrieveOrder.getOrderId());
        if (amount > userOrder.getPayment().doubleValue()) {
            return RestResponse.failed("金额不能大于实际付款金额");
        }

        //添加退款记录
        OrderRefundInfo orderRefunds = new OrderRefundInfo();
        orderRefunds.setOrderId(retrieveOrder.getOrderNo());
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
        String url = "https://api.redadzukibeans.com/weChat/wxpayother/otherRefundOrder?orderNo=" + retrieveOrder.getOrderNo() + "&transactionId=" + userOrder.getTransactionId() + "&subMchId=" + dept.getSubMchId() + "&totalFee=" + userOrder.getPayment().multiply(new BigDecimal(100)).intValue() + "&refundFee=" + new BigDecimal(amount).multiply(new BigDecimal(100)).intValue();

        //String url = "https://api.redadzukibeans.com/weChat/wxpay/otherRefundOrder?orderNo=" + retrieveOrder.getOrderId() + "&transactionId=" + userOrder.getTransactionId() + "&subMchId=" + dept.getSubMchId() + "&totalFee=" + userOrder.getPayment().multiply(new BigDecimal(100)).intValue() + "&refundFee=" + new BigDecimal(amount).multiply(new BigDecimal(100)).intValue();
        String result = HttpUtil.get(url);
        retrieveOrder.setStatus(4);
        retrieveOrderService.updateById(retrieveOrder);
        return RestResponse.ok();

    }

    /**
     * 处理退款回调数据
     *
     * @return
     */
    @ApiOperation(value = "处理退款回调数据")
    @GetMapping("/notifyRefunds")
    public RestResponse notifyRefunds(@RequestParam("resultCode") String resultCode,
                                      @RequestParam("errCodeDes") String errCodeDes,
                                      @RequestParam("outRefundNo") String outRefundNo,
                                      @RequestParam("refundFee") String refundFee,
                                      @RequestParam("refundStatus") String refundStatus) {


        //需要测试一下


        log.info(outRefundNo + "退款订单id");
        log.info(refundStatus + "退款状态");
        log.info(resultCode + "退款resultCode");
        log.info(errCodeDes + "退款errCodeDes");
        OrderRefundInfo orderRefundInfo = orderRefundInfoService.getOne(new QueryWrapper<OrderRefundInfo>().lambda()
                .eq(OrderRefundInfo::getOrderId, outRefundNo));
        if (orderRefundInfo != null) {
            orderRefundInfo.setErrCodeDes(errCodeDes);
            orderRefundInfo.setSuccessTime(new Date());
            orderRefundInfo.setRefundStatus(2);
            orderRefundInfoService.updateById(orderRefundInfo);
            RetrieveOrder retrieveOrder = retrieveOrderService.getOne(new QueryWrapper<RetrieveOrder>().lambda()
                    .eq(RetrieveOrder::getOrderNo, outRefundNo));
            if (refundStatus.equals("SUCCESS")) {
                BigDecimal refundFee1 = orderRefundInfo.getRefundFee();
                BigDecimal divide = refundFee1.divide(new BigDecimal(100));
                if (retrieveOrder != null) {
                    //修改订单的实际回收价
                    retrieveOrder.setRetrieveAmount(divide);

                    retrieveOrder.setStatus(5);
                    retrieveOrderService.updateById(retrieveOrder);

                    UserOrder byId = userOrdertService.getById(Integer.parseInt(retrieveOrder.getOrderId()));

                    UserOrder userOrder = new UserOrder();
                    userOrder.setId(Integer.parseInt(retrieveOrder.getOrderId()));
                    userOrder.setActualRetrieveAmount(divide);
                    userOrder.setSettlementAmount(byId.getPayment().subtract(divide));
                    userOrdertService.updateById(userOrder);
                }
                User userById = userService.getById(retrieveOrder.getUserId());
                //发送公众号通知
                DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                wxMpService.refundNotice(userById.getMpOpenId(), "您的订单已退款", divide + "元", df.format(LocalDateTime.now()), df.format(LocalDateTime.now()),
                        "点击查看详情", "pages/myRetrieveOrder/myRetrieveOrder");

            }

        }
        //图文咨询订单
        PatientOtherOrder patientOtherOrder = patientOtherOrderService.getOne(new QueryWrapper<PatientOtherOrder>().lambda()
                .eq(PatientOtherOrder::getOrderNo, outRefundNo));
        if (patientOtherOrder != null) {
            patientOtherOrder.setStatus(3);
            patientOtherOrderService.updateById(patientOtherOrder);
        }
        return RestResponse.ok();

    }

    public static void main(String[] args) {
        BigDecimal bigDecimal = new BigDecimal(1);
        BigDecimal divide = bigDecimal.divide(new BigDecimal(100));
        System.out.println(divide);
    }

    @ApiOperation(value = "图文咨询订单申请退款")
    @GetMapping("/otherRefundOrder")
    public RestResponse otherRefundOrder(@RequestParam("orderNo") String orderNo) {
        //退款
        PatientOtherOrder patientOtherOrder = patientOtherOrderService.getOne(new QueryWrapper<PatientOtherOrder>().lambda()
                .eq(PatientOtherOrder::getOrderNo, orderNo));

        if (patientOtherOrder.getStatus().equals(2)) {
            Dept dept = deptService.getById(patientOtherOrder.getDeptId());
            String url = "https://api.redadzukibeans.com/weChat/wxpay/otherRefundOrder?orderNo=" + orderNo + "&transactionId=" + patientOtherOrder.getTransactionId() + "&subMchId=" + dept.getSubMchId() + "&totalFee=" + new BigDecimal(patientOtherOrder.getAmount()).multiply(new BigDecimal(100)).intValue() + "&refundFee=" + new BigDecimal(patientOtherOrder.getAmount()).multiply(new BigDecimal(100)).intValue();
            String result = HttpUtil.get(url);
        }
        return RestResponse.ok();

    }

}


