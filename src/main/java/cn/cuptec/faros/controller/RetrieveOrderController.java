package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.com.Url;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.dto.*;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.service.*;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.kuaidi100.sdk.contant.ApiInfoConstant;
import com.kuaidi100.sdk.contant.CompanyConstant;
import com.kuaidi100.sdk.core.IBaseClient;
import com.kuaidi100.sdk.request.PrintReq;
import com.kuaidi100.sdk.request.cloud.COrderReq;
import com.kuaidi100.sdk.utils.SignUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 回收单
 */
@AllArgsConstructor
@Slf4j
@RestController
@RequestMapping("/retrieveOrder")
public class RetrieveOrderController extends AbstractBaseController<RetrieveOrderService, RetrieveOrder> {

    @Resource
    private UserOrdertService userOrdertService;
    @Resource
    private UserService userService;
    @Resource
    private SalesmanRetrieveAddressService salesmanRetrieveAddressService;
    @Resource
    private MobileService mobileService;
    @Resource
    private WxMpService wxMpService;
    @Resource
    private SaleSpecService saleSpecService;//销售规格
    @Resource
    private ServicePackService servicePackService;
    @Resource
    private UserRoleService userRoleService;
    @Resource
    private ServicePackProductPicService servicePackProductPicService;
    @Resource
    private RetrieveOrderReviewDataService retrieveOrderReviewDataService;//回收单审核信息
    @Resource
    private UpdateOrderRecordService updateOrderRecordService;
    @Resource
    private RecyclingRuleService recyclingRuleService;
    @Resource
    private ReviewRefundOrderService reviewRefundOrderService;
    private final Url urlData;

    /**
     * 添加回收单审核设备信息
     */
    @PostMapping("/saveRetrieveOrderReviewData")
    public RestResponse saveRetrieveOrderReviewData(@RequestBody RetrieveOrderReviewData retrieveOrderReviewData) {
        retrieveOrderReviewData.setCreateTime(LocalDateTime.now());
        //修改审核状态
        RetrieveOrder retrieveOrder = new RetrieveOrder();
        retrieveOrder.setId(retrieveOrderReviewData.getRetrieveOrderId());
        retrieveOrder.setStatus(3);
        service.updateById(retrieveOrder);
        RetrieveOrder retrieveOrderOne = service.getById(retrieveOrderReviewData.getRetrieveOrderId());

        UserOrder userOrder = new UserOrder();
        userOrder.setId(Integer.parseInt(retrieveOrderOne.getOrderId()));
        userOrder.setAcceptanceTime(LocalDateTime.now());
        userOrdertService.updateById(userOrder);

        UpdateOrderRecord updateOrderRecord = new UpdateOrderRecord();
        updateOrderRecord.setOrderId(Integer.parseInt(retrieveOrderOne.getOrderId()));
        updateOrderRecord.setCreateUserId(SecurityUtils.getUser().getId());
        updateOrderRecord.setCreateTime(LocalDateTime.now());
        updateOrderRecord.setDescStr("设备审核");
        updateOrderRecordService.save(updateOrderRecord);

        return RestResponse.ok(retrieveOrderReviewDataService.save(retrieveOrderReviewData));
    }

    /**
     * 查询回收单审核信息
     *
     * @param retrieveOrderId
     * @return
     */
    @GetMapping("/getRetrieveOrderReviewDataById")
    public RestResponse getRetrieveOrderReviewDataById(@RequestParam("retrieveOrderId") Integer retrieveOrderId) {

        return RestResponse.ok(retrieveOrderReviewDataService.getOne(new QueryWrapper<RetrieveOrderReviewData>().lambda().eq(RetrieveOrderReviewData::getRetrieveOrderId, retrieveOrderId)));
    }

    /**
     * 查询部门下的回收单
     *
     * @return
     */
    @GetMapping("/manage/pageScoped")
    public RestResponse pageScoped(@RequestParam(value = "servicePackName", required = false) String servicePackName,
                                   @RequestParam(value = "startTime", required = false) String startTime,
                                   @RequestParam(value = "endTime", required = false) String endTime,
                                   @RequestParam(value = "nickname", required = false) String nickname,
                                   @RequestParam(value = "receiverPhone", required = false) String receiverPhone,
                                   @RequestParam(value = "orderId", required = false) String orderId,
                                   @RequestParam(value = "userOrderNo", required = false) String userOrderNo,
                                   @RequestParam(value = "retrieveOrderNo", required = false) String retrieveOrderNo) {
        Page<RetrieveOrder> page = getPage();
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());
        if (!StringUtils.isEmpty(servicePackName)) {
            queryWrapper.eq("service_pack.name", servicePackName);
        }
        if (!StringUtils.isEmpty(retrieveOrderNo)) {
            queryWrapper.eq("retrieve_order.order_no", retrieveOrderNo);
        }
        if (!StringUtils.isEmpty(nickname)) {
            queryWrapper.eq("patient_user.name", nickname);
        }
        if (!StringUtils.isEmpty(orderId)) {
            queryWrapper.eq("retrieve_order.order_id", orderId);
        }
        if (!StringUtils.isEmpty(userOrderNo)) {
            queryWrapper.eq("retrieve_order.user_order_no", userOrderNo);
        }
        if (!StringUtils.isEmpty(startTime)) {
            if (StringUtils.isEmpty(endTime)) {
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                endTime = df.format(now);
            }
            queryWrapper.le("retrieve_order.create_time", endTime);
            queryWrapper.ge("retrieve_order.create_time", startTime);
        }
        Boolean aBoolean = userRoleService.judgeUserIsAdmin(SecurityUtils.getUser().getId());

        IPage pageResult = service.pageScoped(aBoolean, page, queryWrapper);
        if (CollUtil.isNotEmpty(pageResult.getRecords())) {
            List<RetrieveOrder> records = pageResult.getRecords();

            //查询退款审核记录
            List<Integer> reviewRefundOrderIds = new ArrayList<>();
            for (RetrieveOrder retrieveOrder : records) {
                if (retrieveOrder.getReviewRefundOrderId() != null) {
                    reviewRefundOrderIds.add(retrieveOrder.getReviewRefundOrderId());
                }
            }
            Map<Integer, ReviewRefundOrder> reviewRefundOrderMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(reviewRefundOrderIds)) {
                List<ReviewRefundOrder> reviewRefundOrders = (List<ReviewRefundOrder>) reviewRefundOrderService.listByIds(reviewRefundOrderIds);


                reviewRefundOrderMap = reviewRefundOrders.stream()
                        .collect(Collectors.toMap(ReviewRefundOrder::getId, t -> t));
            }

            //服务包信息
            List<Integer> servicePackIds = records.stream().map(RetrieveOrder::getServicePackId)
                    .collect(Collectors.toList());
            List<ServicePack> servicePacks = (List<ServicePack>) servicePackService.listByIds(servicePackIds);
            //查询服务包图片信息
            if (!org.springframework.util.CollectionUtils.isEmpty(servicePacks)) {
                List<ServicePackProductPic> servicePackProductPics = servicePackProductPicService.list(new QueryWrapper<ServicePackProductPic>().lambda()
                        .in(ServicePackProductPic::getServicePackId, servicePackIds));
                if (!org.springframework.util.CollectionUtils.isEmpty(servicePackProductPics)) {
                    Map<Integer, List<ServicePackProductPic>> servicePackProductPicMap = servicePackProductPics.stream()
                            .collect(Collectors.groupingBy(ServicePackProductPic::getServicePackId));
                    for (ServicePack servicePack : servicePacks) {
                        List<ServicePackProductPic> servicePackProductPics1 = servicePackProductPicMap.get(servicePack.getId());
                        if (!org.springframework.util.CollectionUtils.isEmpty(servicePackProductPics1)) {
                            servicePack.setServicePackProductPics(servicePackProductPics1);
                        } else {
                            servicePack.setServicePackProductPics(new ArrayList<>());
                        }
                    }
                }
            }

            Map<Integer, ServicePack> servicePackMap = servicePacks.stream()
                    .collect(Collectors.toMap(ServicePack::getId, t -> t));
            for (RetrieveOrder retrieveOrder : records) {
                ServicePack servicePack = servicePackMap.get(retrieveOrder.getServicePackId());
                if (servicePack == null) {
                    servicePack = new ServicePack();
                }
                List<ServicePackProductPic> servicePackProductPics = servicePack.getServicePackProductPics();
                if (org.springframework.util.CollectionUtils.isEmpty(servicePackProductPics)) {
                    servicePack.setServicePackProductPics(new ArrayList<>());
                }
                retrieveOrder.setServicePack(servicePack);
                if (retrieveOrder.getReviewRefundOrderId() != null) {
                    ReviewRefundOrder reviewRefundOrder = reviewRefundOrderMap.get(retrieveOrder.getReviewRefundOrderId());
                    retrieveOrder.setReviewRefundOrder(reviewRefundOrder);
                }
            }
        }

        return RestResponse.ok(pageResult);
    }

    @GetMapping("/manage/countScoped")
    public RestResponse countScoped() {
        return RestResponse.ok(service.countScoped());
    }

    /**
     * 用户查询自己的回收单
     *
     * @return
     */
    @GetMapping("/user/pageMy")
    public RestResponse listMy() {
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());
        Page<RetrieveOrder> page = getPage();
        queryWrapper.eq("user_id", SecurityUtils.getUser().getId());
        queryWrapper.orderByDesc("create_time");


        return RestResponse.ok(service.page(page, queryWrapper));
    }

    @GetMapping("/user/orderDetail")
    public RestResponse getMyOrderDetail(@RequestParam int id) {
        RetrieveOrder retrieveOrder = service.getById(id);
        //业务员的联系方式及回收地址
        if (retrieveOrder.getSalesmanId() != null) {
            User user = userService.getById(retrieveOrder.getSalesmanId());
            retrieveOrder.setSalesmanName(user.getNickname());
            retrieveOrder.setSalesmanPhone(user.getPhone());
            SalesmanRetrieveAddress retrieveAddress = salesmanRetrieveAddressService.getOne(Wrappers.<SalesmanRetrieveAddress>lambdaQuery()
                    .eq(SalesmanRetrieveAddress::getSalesmanId, retrieveOrder.getSalesmanId())
            );
            retrieveOrder.setReceiverName(user.getNickname());
            retrieveOrder.setReceiverPhone(user.getPhone());
            if (retrieveAddress != null) {
                retrieveOrder.setReceiverRegion(retrieveAddress.getRetrieveRegion());
                retrieveOrder.setReceiverDetailAddress(retrieveAddress.getRetrieveDetailAddress());
            }

        }


        retrieveOrder.setPaymentMethod(new Integer[]{1});
        if (retrieveOrder.getId() != null) {
            UserOrder byId = userOrdertService.getById(retrieveOrder.getId());

        }


        return RestResponse.ok(retrieveOrder);
    }

    //修改回收价格
    @PutMapping("/manage/modifyRetrieveAmount")
    public RestResponse modifyRetrieveAmount(@RequestParam int id, @RequestParam BigDecimal actualRetrieveAmount) {
        service.modifyRetrieveAmount(id, actualRetrieveAmount);
        RetrieveOrder retrieveOrder = service.getById(id);
        //发送公众号消息提醒
        User user = userService.getById(retrieveOrder.getUserId());
        if (user != null) {
            if (!StringUtils.isEmpty(user.getMpOpenId())) {
                DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime time = LocalDateTime.now();
                String localTime = df.format(time);
                String url = urlData.getUrl() + "index.html#/ucenter/retrieveOrder/" + retrieveOrder.getId();
                wxMpService.sendTopic(user.getMpOpenId(), "订单状态变更", localTime, "业务员修改回收价格", "待您确认", url);
            }
        }

        return RestResponse.ok();
    }

    /**
     * 用户一键寄回 生成回收单
     */
    @PostMapping("/user/saveRetrieveOrder")
    public RestResponse saveRetrieveOrder(@RequestBody RetrieveOrder retrieveOrder) {
        retrieveOrder.setUserId(SecurityUtils.getUser().getId());
        retrieveOrder.setCreateTime(new Date());
        retrieveOrder.setOrderNo(IdUtil.getSnowflake(0, 0).nextIdStr());
        retrieveOrder.setStatus(1);
        Long day = 1L;
        String orderId = retrieveOrder.getOrderId();
        UserOrder userOrder = userOrdertService.getById(orderId);
        if (userOrder != null) {
            Date deliveryTime = userOrder.getDeliveryTime();
            Instant instant = deliveryTime.toInstant();
            ZoneId zoneId = ZoneId.systemDefault();

            LocalDateTime localDateDeliveryTime = instant.atZone(zoneId).toLocalDateTime();

            LocalDateTime now = LocalDateTime.now();
            java.time.Duration duration = java.time.Duration.between(localDateDeliveryTime, now);
            day = duration.toDays();

        }
        retrieveOrder.setRentDay(day.intValue());
        service.saveRetrieveOrder(retrieveOrder);
        userOrder.setStatus(5);
        userOrdertService.updateById(userOrder);
        return RestResponse.ok();
    }

    //用户确认订单回收价格
    @PutMapping("/user/confirmRetrieveAmount")
    public RestResponse confirmRetrieveAmount(@RequestParam int id) {
        service.confirmRetrieveAmount(id);
        return RestResponse.ok();
    }


    //计算退款金额
    @GetMapping("/retrieveAmount")
    public RestResponse retrieveAmount(@RequestParam("id") int id) {
        RetrieveAmountDto retrieveAmountDto = new RetrieveAmountDto();
        RetrieveOrder retrieveOrder = service.getById(id);
        UserOrder userOrder = userOrdertService.getById(retrieveOrder.getOrderId());
        if (userOrder.getStatus().equals(7)) {
            retrieveAmountDto.setAmount(userOrder.getPayment());
            retrieveAmountDto.setReviewData("订单已被取消");
            retrieveAmountDto.setTotalAmount(userOrder.getPayment());
            retrieveAmountDto.setPayTime(userOrder.getPayTime());
            return RestResponse.ok(retrieveAmountDto);
        }
        Integer rentDay = retrieveOrder.getRentDay();
        retrieveAmountDto.setRentDay(rentDay);
        BigDecimal amount = new BigDecimal(0);
        String recyclingRuleList1 = userOrder.getRecyclingRuleList();

        if (!StringUtils.isEmpty(recyclingRuleList1)) {
            List<String> ids = Arrays.asList(recyclingRuleList1.split("/"));
            List<RecyclingRule> recyclingRuleList = (List<RecyclingRule>) recyclingRuleService.listByIds(ids);
            if (!CollectionUtils.isEmpty(recyclingRuleList)) {
                Collections.sort(recyclingRuleList);
                for (RecyclingRule recyclingRule : recyclingRuleList) {
                    Integer day = recyclingRule.getDay();
                    if (rentDay <= day) {
                        amount = recyclingRule.getAmount();
                        break;
                    }

                }
            }


        } else {
            if (userOrder.getSaleSpecRecoveryPrice() != null) {
                amount = new BigDecimal(userOrder.getSaleSpecRecoveryPrice() + "");

            }

        }

        Date date = userOrder.getDeliveryTime();
        Instant instant = date.toInstant();
        ZoneId zoneId = ZoneId.systemDefault();
        ZonedDateTime zonedDateTime = instant.atZone(zoneId);
        LocalDateTime startTime = zonedDateTime.toLocalDateTime();
        retrieveAmountDto.setAmount(amount);
        retrieveAmountDto.setPayTime(userOrder.getPayTime());
        retrieveAmountDto.setTotalAmount(userOrder.getPayment());
        retrieveAmountDto.setStartTime(startTime);
        if (userOrder.getSaleSpecServiceEndTime() != null) {
            retrieveAmountDto.setEndTime(startTime.plusDays(userOrder.getSaleSpecServiceEndTime()));
        }

        List<RetrieveOrderReviewData> list = retrieveOrderReviewDataService.list(new QueryWrapper<RetrieveOrderReviewData>().lambda()
                .eq(RetrieveOrderReviewData::getRetrieveOrderId, id));
        if (!CollectionUtils.isEmpty(list)) {
            retrieveAmountDto.setReviewData(list.get(0).getReviewData());
        }
        //查询退款审核描述
        List<ReviewRefundOrder> reviewRefundOrders = reviewRefundOrderService.list(new QueryWrapper<ReviewRefundOrder>().lambda()
                .eq(ReviewRefundOrder::getRetrieveOrderNo, retrieveOrder.getOrderNo()));
        retrieveAmountDto.setReviewRefundOrders(reviewRefundOrders);
        return RestResponse.ok(retrieveAmountDto);
    }


    //延长收货
    @GetMapping("/user/extendedReceipt")
    public RestResponse extendedReceipt(@RequestParam("id") int id, @RequestParam("day") int day) {
        RetrieveOrder retrieveOrder = service.getById(id);
        Date automaticCreateTime = retrieveOrder.getAutomaticCreateTime();
        if (automaticCreateTime == null) {
            automaticCreateTime = retrieveOrder.getCreateTime();
        }
        retrieveOrder.setStatus(3);
        Instant instant = automaticCreateTime.toInstant();
        ZoneId zoneId = ZoneId.systemDefault();
        LocalDateTime localDate = instant.atZone(zoneId).toLocalDateTime();
        LocalDateTime localDateTime = localDate.plusDays(day);
        ZonedDateTime zdt = localDateTime.atZone(zoneId);
        Date date = Date.from(zdt.toInstant());
        retrieveOrder.setAutomaticCreateTime(date);
        service.updateById(retrieveOrder);
        return RestResponse.ok("延长成功!");
    }

    /**
     * 用户查询自己的回收单数量
     */
    @GetMapping("/listRetrieveOrderMyStateCount")
    public RestResponse listRetrieveOrderMyStateCount() {
        MyStateCount myStateCount = new MyStateCount();
        myStateCount.setPendingPayment(service.count(Wrappers.<RetrieveOrder>lambdaQuery()
                .eq(RetrieveOrder::getUserId, SecurityUtils.getUser().getId())
                .eq(RetrieveOrder::getStatus, 1)));//待收货
        myStateCount.setPendingDelivery(service.count(Wrappers.<RetrieveOrder>lambdaQuery()
                .eq(RetrieveOrder::getUserId, SecurityUtils.getUser().getId())
                .eq(RetrieveOrder::getStatus, 2))); //待审核
        myStateCount.setPendingReward(service.count(Wrappers.<RetrieveOrder>lambdaQuery()
                .eq(RetrieveOrder::getUserId, SecurityUtils.getUser().getId())
                .eq(RetrieveOrder::getStatus, 3)));//待大款
        myStateCount.setUsedCount(service.count(Wrappers.<RetrieveOrder>lambdaQuery()
                .eq(RetrieveOrder::getUserId, SecurityUtils.getUser().getId())
                .eq(RetrieveOrder::getStatus, 4)));//待收款
        myStateCount.setPendingRecycle(service.count(Wrappers.<RetrieveOrder>lambdaQuery()
                .eq(RetrieveOrder::getUserId, SecurityUtils.getUser().getId())
                .eq(RetrieveOrder::getStatus, 5)));//回收完成
        return RestResponse.ok(myStateCount);
    }


    //用户确认邮寄
    @PutMapping("/user/confirmDelivery")
    public RestResponse confirmDelivery(@RequestBody RetrieveOrder retrieveOrder) {
        service.confirmDelivery(retrieveOrder.getId(), retrieveOrder.getDeliveryCompanyCode(), retrieveOrder.getDeliveryCompanyName(), retrieveOrder.getDeliverySn());

        retrieveOrder = service.getById(retrieveOrder.getId());
        //发送公众号消息提醒 业务员
        User user = userService.getById(retrieveOrder.getSalesmanId());
        if (user != null) {
            if (!StringUtils.isEmpty(user.getMpOpenId())) {
                DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime time = LocalDateTime.now();
                String localTime = df.format(time);
                String url = urlData.getUrl() + "index.html#/ucenter/retrieveOrder/" + retrieveOrder.getId();
                wxMpService.sendTopic(user.getMpOpenId(), "订单状态变更", localTime, "用户确认邮寄", "用户确认邮寄", url);
            }
        }
        return RestResponse.ok();
    }

    @GetMapping("getByProductSn")
    public RestResponse getByProductSn(@RequestParam("productSn") String productSn) {
        List<RetrieveOrder> list = service.list(new QueryWrapper<RetrieveOrder>().lambda().eq(RetrieveOrder::getProductSn, productSn).eq(RetrieveOrder::getStatus, 1).orderByDesc(RetrieveOrder::getCreateTime));
        if (!CollectionUtils.isEmpty(list)) {
            return RestResponse.ok(list.get(0));
        }
        return RestResponse.failed("未查询到回收单");
    }

    //业务员确认已收货
    @GetMapping("/manage/confirmReceived")
    public RestResponse confirmReceived(@RequestParam int id) {
        service.confirmReceived(id);

        return RestResponse.ok();
    }

    @GetMapping("/testSendMessage")
    public void testSendMessage() {
        mobileService.sendRetrievedMoneySms("13307637744");
    }

    //业务员确认打款
    @PutMapping("/manage/confirmPostMoney")
    public RestResponse confirmPostMoney(@RequestParam int id) {
        service.confirmPostMoney(id);
        RetrieveOrder retrieveOrder = service.getById(id);
        //发送公众号消息提醒
        User user = userService.getById(retrieveOrder.getUserId());
        if (user != null) {
            if (!StringUtils.isEmpty(user.getMpOpenId())) {
                DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime time = LocalDateTime.now();
                String localTime = df.format(time);
                String url = urlData.getUrl() + "index.html#/ucenter/retrieveOrder/" + retrieveOrder.getId();
                wxMpService.sendTopic(user.getMpOpenId(), "订单状态变更", localTime, "业务员已打款", "待您确认", url);
            }
        }
        return RestResponse.ok();
    }

    @PutMapping("/user/userConfirmRetrieved")
    public RestResponse userConfirmRevMoney(@RequestParam int id) {
        service.confirmRetrieved(id);


        return RestResponse.ok();
    }

    //用戶确认收款发送短信
    @PutMapping("/manage/userConfirmPostMoneySendMessage")
    public RestResponse userConfirmPostMoneySendMessage(@RequestParam int id) {
        //发送短信给业务员
        RetrieveOrder retrieveOrder = service.getById(id);
        if (retrieveOrder == null) {
            return RestResponse.ok();
        }
        try {
            mobileService.sendUserRetrievedMoneySms(retrieveOrder.getReceiverPhone(), retrieveOrder.getProductSn());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RestResponse.ok();
    }

    @PutMapping("/manage/confirmRetrieved/{id}")
    public RestResponse confirmRetrieved(@PathVariable int id) {
        service.confirmRetrieved(id);
        RetrieveOrder retrieveOrder = service.getById(id);
        //发送公众号消息提醒
        User user = userService.getById(retrieveOrder.getUserId());
        if (user != null) {
            if (!StringUtils.isEmpty(user.getMpOpenId())) {
                DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                LocalDateTime time = LocalDateTime.now();
                String localTime = df.format(time);
                String url = "http://pharos.ewj100.com/index.html#/ucenter/retrieveOrder/" + retrieveOrder.getId();
                wxMpService.sendTopic(user.getMpOpenId(), "订单状态变更", localTime, "回收完成", "谢谢", url);
            }
        }
        return RestResponse.ok();
    }


    @GetMapping("/manage/canRefundList")
    public RestResponse canRefundList() {
        Page<RetrieveOrder> page = getPage();

        return RestResponse.ok(service.canRefundList(page));
    }
//    @DeleteMapping("{id}")
//    public RestResponse deleteById(@PathVariable int id){
//        service.removeById(id);
//        return RestResponse.ok();
//    }


    @GetMapping("/getNewBySn")
    public RestResponse getNewBySn(@RequestParam("productSn") String productSn) {
        Page<RetrieveOrder> page = getPage();

        return RestResponse.ok(service.getOne(new QueryWrapper<RetrieveOrder>().lambda().eq(RetrieveOrder::getProductSn, productSn).orderByDesc(RetrieveOrder::getId).last(" limit 1")));
    }

    /**
     * 回收单自动下单 叫物流
     *
     * @param param
     * @return
     * @throws Exception
     */
    @PostMapping("xiadan")
    public RestResponse XiaDan(@RequestBody KuaiDiXiaDanParam param) throws Exception {

        Map<String, String> params = new HashMap();
        params.put("secret_key", "C58ZzLwXbQu6hqSHvz");
        params.put("secret_code", "ddffadd3df4b4c0d8d6f9942c7a8c990");
        params.put("secret_sign", "2E20DED9F6AC5E211E84A80A4E13FAC8");
        params.put("com", param.getCom());
        params.put("recManName", param.getRecManName());
        params.put("recManMobile", param.getRecManMobile());
        params.put("recManPrintAddr", param.getRecManPrintAddr());
        params.put("sendManName", param.getSendManName());
        params.put("sendManMobile", param.getSendManMobile());
        params.put("sendManPrintAddr", param.getSendManPrintAddr());
        params.put("cargo", param.getCargo());
        params.put("weight", param.getWeight());
        params.put("remark", param.getRemark());
        params.put("salt", "123456");
        params.put("callBackUrl", urlData.getUrl() + "retrieveOrder/kuaidicallback");
        params.put("dayType", param.getDayType());
        params.put("pickupStartTime", param.getPickupStartTime());
        params.put("pickupEndTime", param.getPickupEndTime());


        String post = post(params);
        log.info("回收返回结果" + post);

        XiaDanParam xiaDanParam = new Gson().fromJson(post, XiaDanParam.class);
        if (xiaDanParam.getCode() == 200 && xiaDanParam.getMessage().equals("success")) {
            //计算回收天数
            String orderId = param.getOrderNo();
            UserOrder userOrder = userOrdertService.getById(orderId);
            Long day = 1L;
            if (userOrder != null) {
                Date deliveryTime = userOrder.getDeliveryTime();
                Instant instant = deliveryTime.toInstant();
                ZoneId zoneId = ZoneId.systemDefault();

                LocalDateTime localDateDeliveryTime = instant.atZone(zoneId).toLocalDateTime();

                LocalDateTime now = LocalDateTime.now();
                java.time.Duration duration = java.time.Duration.between(localDateDeliveryTime, now);
                day = duration.toDays();
                userOrder.setUseDay(day.intValue());
                userOrdertService.updateById(userOrder);
            }
            RetrieveOrder retrieveOrder = new RetrieveOrder();
            retrieveOrder.setUserOrderNo(userOrder.getOrderNo());
            retrieveOrder.setRentDay(day.intValue());
            retrieveOrder.setOrderId(param.getOrderNo());
            retrieveOrder.setUserId(SecurityUtils.getUser().getId());
            retrieveOrder.setCreateTime(new Date());
            retrieveOrder.setOrderNo(IdUtil.getSnowflake(0, 0).nextIdStr());
            retrieveOrder.setStatus(1);
            retrieveOrder.setReceiverPhone(param.getRecManMobile());
            retrieveOrder.setTaskId(xiaDanParam.getData().getTaskId());
            retrieveOrder.setDeliveryCompanyCode(param.getCom());
            service.saveRetrieveOrder(retrieveOrder);
            userOrder.setStatus(5);
            userOrdertService.updateById(userOrder);
            return RestResponse.ok();
        }
        return RestResponse.failed(xiaDanParam.getMessage());
    }

    /**
     * 回收单物流订阅回掉
     *
     * @return
     * @throws Exception
     */
    @PostMapping("kuaidicallback")
    public KuaiDiCallBackResult kuaidicallback(HttpServletRequest request) throws Exception {
        String param = request.getParameter("param");
        String taskId = request.getParameter("taskId");
        log.info("快递回调快递回调快递回调快递回调快递回调快递回调快递回调快递回调快递回调:{}", param);

        KuaiDiCallBackParam kuaiDiCallBackParam = new Gson().fromJson(param, KuaiDiCallBackParam.class);
        RetrieveOrder one = service.getOne(new QueryWrapper<RetrieveOrder>().lambda().eq(RetrieveOrder::getTaskId, taskId));
        one.setKuAiDiStatus(Integer.parseInt(kuaiDiCallBackParam.getData().getStatus()));
        one.setDeliverySn(kuaiDiCallBackParam.getKuaidinum());
        service.updateById(one);
        //判断快递状态 修改状态为待审核
        if (kuaiDiCallBackParam.getData().getStatus().equals("13")) {
            one.setStatus(2);
            service.updateById(one);
        }
        if (kuaiDiCallBackParam.getData().getStatus().equals("10")) {
            UserOrder userOrder = new UserOrder();
            userOrder.setId(Integer.parseInt(one.getOrderId()));
            userOrder.setRecycleTime(LocalDateTime.now());
            userOrdertService.updateById(userOrder);
        }
        KuaiDiCallBackResult kuaiDiCallBackResult = new KuaiDiCallBackResult();
        kuaiDiCallBackResult.setResult(true);
        kuaiDiCallBackResult.setMessage("成功");
        kuaiDiCallBackResult.setReturnCode("200");
        return kuaiDiCallBackResult;
    }

    public static String post(Map<String, String> params) {
        StringBuilder response = new StringBuilder("");
        BufferedReader reader = null;
        try {
            StringBuilder builder = new StringBuilder();
            for (Map.Entry param : params.entrySet()) {
                if (builder.length() > 0) {
                    builder.append('&');
                }
                builder.append(URLEncoder.encode(param.getKey() + "", "UTF-8"));
                builder.append('=');
                builder.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
            }
            byte[] bytes = builder.toString().getBytes("UTF-8");
            URL url = new URL("http://cloud.kuaidi100.com/api");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestProperty("Content-Length", String.valueOf(bytes.length));
            conn.setDoOutput(true);
            conn.getOutputStream().write(bytes);
            reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line = "";
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (null != reader) {
                    reader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return response.toString();
    }

    public static void main(String[] args) {


        Map<String, String> params = new HashMap();
        params.put("secret_key", "C58ZzLwXbQu6hqSHvz");
        params.put("secret_code", "ddffadd3df4b4c0d8d6f9942c7a8c990");
        params.put("secret_sign", "2E20DED9F6AC5E211E84A80A4E13FAC8");
        params.put("com", CompanyConstant.SF);
        params.put("recManName", "李四");
        params.put("recManMobile", "13916908294");
        params.put("recManPrintAddr", "中国上海上海市闵行区剑川路930弄C座1楼");
        params.put("sendManName", "张三");
        params.put("sendManMobile", "18709108132");
        params.put("sendManPrintAddr", "上海市上海市静安区上海市");
        params.put("cargo", "文件");
        params.put("weight", "1");
        params.put("remark", "测试下单，待会取消");
        params.put("salt", "123456");
        params.put("callBackUrl", "https://pharos3.ewj100.com/retrieveOrder/kuaidicallback");
        params.put("dayType", "今天");
        params.put("pickupStartTime", "18:00");
        params.put("pickupEndTime", "20:00");


        String post = post(params);
        System.out.println(post);
    }

    @Override
    protected Class<RetrieveOrder> getEntityClass() {
        return RetrieveOrder.class;
    }
}
