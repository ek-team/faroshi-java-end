package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.common.utils.QrCodeUtil;
import cn.cuptec.faros.common.utils.http.ServletUtils;
import cn.cuptec.faros.config.com.Url;
import cn.cuptec.faros.config.oss.OssProperties;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.dto.CalculatePriceResult;
import cn.cuptec.faros.dto.KuaiDiCallBackResult;
import cn.cuptec.faros.dto.MyStateCount;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.service.*;
import cn.cuptec.faros.util.ExcelUtil;
import cn.cuptec.faros.util.UploadFileUtils;
import cn.cuptec.faros.vo.UOrderStatuCountVo;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.excel.EasyExcel;
import com.aliyun.oss.OSS;
import com.aliyun.oss.model.PutObjectResult;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.kuaidi100.sdk.response.SubscribePushParamResp;
import com.kuaidi100.sdk.response.SubscribeResp;
import com.kuaidi100.sdk.utils.SignUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.*;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/purchase/order")
public class UserOrderController extends AbstractBaseController<UserOrdertService, UserOrder> {
    private final OssProperties ossProperties;
    @Resource
    private UserRoleService userRoleService;
    @Resource
    private UpdateOrderRecordService updateOrderRecordService;
    @Resource
    private PatientUserService patientUserService;
    @Resource
    private FormUserDataService formUserDataService;
    @Resource
    private ServicePackageInfoService servicePackageInfoService;
    @Resource
    private ServicePackService servicePackService;
    @Resource
    private HospitalInfoService hospitalInfoService;
    @Resource
    private ServicePackProductPicService servicePackProductPicService;
    @Resource
    private UserServicePackageInfoService userServicePackageInfoService;
    @Resource
    private WxPayFarosService wxPayFarosService;
    @Resource
    private AddressService addressService;
    @Resource
    private UserService userService;
    @Resource
    private SaleSpecGroupService saleSpecGroupService;
    @Resource
    private SaleSpecDescService saleSpecDescService;
    @Resource
    private SaleSpecService saleSpecService;
    @Resource
    private DoctorTeamService doctorTeamService;
    @Resource
    private cn.cuptec.faros.service.WxMpService wxMpService;
    @Resource
    private RetrieveOrderService retrieveOrderService;
    @Resource
    private RentRuleOrderService rentRuleOrderService;
    @Resource
    private RentRuleService rentRuleService;
    @Resource
    private PlanUserService planUserService;
    @Resource
    private OrderRefundInfoService orderRefundInfoService;
    @Resource
    private ProductStockService productStockService;
    @Resource
    private DeviceScanSignLogService deviceScanSignLogService;
    private final Url urlData;

    /**
     * 查询订单的续租记录
     */
    @GetMapping("/queryRentRuleOrder")
    public RestResponse queryRentRuleOrder(
            @RequestParam("userOrderNo") String userOrderNo) {
        String[] split = userOrderNo.split("-");
        if (split.length == 1) {
            userOrderNo = split[0];
        } else {
            userOrderNo = split[1];
        }
        List<RentRuleOrder> list = rentRuleOrderService.list(new QueryWrapper<RentRuleOrder>().lambda().eq(RentRuleOrder::getUserOrderNo, userOrderNo));


        return RestResponse.ok(list);
    }

    /**
     * 续租列表分页查询
     *
     * @return
     */
    @GetMapping("/pageRentRuleOrder")
    public RestResponse pageRentRuleOrder(@RequestParam("pageNum") int pageNum, @RequestParam("pageSize") int pageSize) {
        IPage page = new Page(pageNum, pageSize);
        IPage page1 = rentRuleOrderService.page(page, new QueryWrapper<RentRuleOrder>().lambda().eq(RentRuleOrder::getUserId, SecurityUtils.getUser().getId()));


        return RestResponse.ok(page1);
    }


    /**
     * 订单续租
     */
    @GetMapping("/rentRuleOrder")
    public RestResponse rentRuleOrder(@RequestParam("rentRuleId") Integer rentRuleId,
                                      @RequestParam("userOrderNo") String userOrderNo) {
        RentRule rentRule = rentRuleService.getById(rentRuleId);
        String[] split = userOrderNo.split("-");
        if (split.length == 1) {
            userOrderNo = split[0];
        } else {
            userOrderNo = split[1];
        }
        UserOrder userOrder = service.getOne(new QueryWrapper<UserOrder>().lambda().eq(UserOrder::getOrderNo, userOrderNo));

        RentRuleOrder rentRuleOrder = new RentRuleOrder();
        rentRuleOrder.setUserOrderNo(userOrderNo);
        rentRuleOrder.setServiceCount(rentRule.getServiceCount());
        rentRuleOrder.setRentRuleOrderNo(IdUtil.getSnowflake(0, 0).nextIdStr());
        rentRuleOrder.setAmount(rentRule.getAmount());
        rentRuleOrder.setDay(rentRule.getDay() + "");
        rentRuleOrder.setUserId(SecurityUtils.getUser().getId());
        rentRuleOrder.setStatus(1);
        rentRuleOrder.setCreateTime(LocalDateTime.now());
        rentRuleOrderService.save(rentRuleOrder);
        RestResponse restResponse = wxPayFarosService.unifiedOrder(userOrder.getDeptId(), rentRuleOrder.getRentRuleOrderNo(), null, rentRuleOrder.getAmount());

        return restResponse;
    }

    /**
     * 获取省的订单数量
     *
     * @return
     */
    @GetMapping("/getOrderProvinceCount")
    public RestResponse getOrderProvinceCount() {

//        List<UserOrder> list = service.list(new QueryWrapper<UserOrder>().lambda().notIn(UserOrder::getStatus, 0, 1, 6).isNotNull(UserOrder::getProvince));
//        if (!CollectionUtils.isEmpty(list)) {
//            Map<String, List<UserOrder>> map = list.stream()
//                    .collect(Collectors.groupingBy(UserOrder::getProvince));
//            return RestResponse.ok(map);
//        }

        return RestResponse.ok();
    }

    /**
     * 物流订阅回掉
     *
     * @param request
     * @return
     * @throws Exception
     */
    @PostMapping("subscribe_Callback")
    public SubscribeResp subscribe_Callback(HttpServletRequest request) throws Exception {
        String param = request.getParameter("param");
        String sign = request.getParameter("sign");
        //建议记录一下这个回调的内容，方便出问题后双方排查问题
        log.info("快递100订阅推送回调结果|", param);
        //订阅时传的salt,没有可以忽略
        String salt = null;
        String ourSign = SignUtils.sign(param + salt);
        SubscribeResp subscribeResp = new SubscribeResp();
        subscribeResp.setResult(Boolean.TRUE);
        subscribeResp.setReturnCode("200");
        subscribeResp.setMessage("成功");
        //加密如果相等，属于快递100推送；否则可以忽略掉当前请求
        log.info("进入业务处理");
        SubscribePushParamResp subscribePushParamResp = new Gson().fromJson(param, SubscribePushParamResp.class);
        //TODO 业务处理


        List<UserOrder> userOrders = service.list(new QueryWrapper<UserOrder>().lambda()
                .eq(UserOrder::getDeliveryCompanyCode, subscribePushParamResp.getLastResult().getCom())
                .eq(UserOrder::getDeliverySn, subscribePushParamResp.getLastResult().getNu()));
        if ("shutdown".equals(subscribePushParamResp.getStatus())) {

            // 修改状态为收货
            if (!CollectionUtils.isEmpty(userOrders)) {
                for (UserOrder userOrder : userOrders) {
                    userOrder.setStatus(4);
                    userOrder.setRevTime(LocalDateTime.now());
                }
                service.updateBatchById(userOrders);
            }
        }

        return subscribeResp;

    }

    /**
     * 获取市的订单数量
     *
     * @return
     */
    @GetMapping("/getOrderCityCount")
    public RestResponse getOrderCityCount(@RequestParam("province") String province) {

//        List<UserOrder> list = service.list(new QueryWrapper<UserOrder>().lambda().notIn(UserOrder::getStatus, 0, 1, 6).isNotNull(UserOrder::getCity).like(UserOrder::getProvince, province));
//        if (!CollectionUtils.isEmpty(list)) {
//            Map<String, List<UserOrder>> map = list.stream()
//                    .collect(Collectors.groupingBy(UserOrder::getCity));
//            return RestResponse.ok(map);
//        }

        return RestResponse.ok();
    }


    //查询部门订单列表,只允许查看
    @GetMapping("/manage/pageScoped")
    public RestResponse pageScoped(@RequestParam(value = "servicePackName", required = false) String servicePackName,
                                   @RequestParam(value = "startTime", required = false) String startTime,
                                   @RequestParam(value = "endTime", required = false) String endTime,
                                   @RequestParam(value = "nickname", required = false) String nickname,
                                   @RequestParam(value = "receiverPhone", required = false) String receiverPhone,
                                   @RequestParam(value = "toSort", required = false) String toSort,
                                   @RequestParam(value = "userOrderNo", required = false) String userOrderNo,
                                   @RequestParam(value = "productSn1", required = false) String productSn1,
                                   @RequestParam(value = "productSn2", required = false) String productSn2,
                                   @RequestParam(value = "productSn3", required = false) String productSn3,
                                   @RequestParam(value = "startDeliveryDate", required = false) String startDeliveryDate) {
        Page<UserOrder> page = getPage();
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());
        if (!StringUtils.isEmpty(servicePackName)) {
            queryWrapper.eq("service_pack.name", servicePackName);
        }
        if (!StringUtils.isEmpty(nickname)) {
            queryWrapper.eq("patient_user.name", nickname);
        }
        if (!StringUtils.isEmpty(receiverPhone)) {
            queryWrapper.eq("user_order.receiver_phone", receiverPhone);
        }
        if (!StringUtils.isEmpty(userOrderNo)) {
            String[] split = userOrderNo.split("-");
            if (split.length == 1) {
                userOrderNo = split[0];
            } else {
                userOrderNo = split[1];
            }
            queryWrapper.like("user_order.order_no", userOrderNo);
        }
        if (!StringUtils.isEmpty(productSn1)) {
            queryWrapper.like("user_order.product_sn1", productSn1);
        }
        if (!StringUtils.isEmpty(productSn2)) {
            queryWrapper.like("user_order.product_sn2", productSn2);
        }
        if (!StringUtils.isEmpty(productSn3)) {
            queryWrapper.like("user_order.product_sn3", productSn3);
        }
        if (!StringUtils.isEmpty(startTime)) {
            if (StringUtils.isEmpty(endTime)) {
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                endTime = df.format(now);
            }
            queryWrapper.le("user_order.create_time", endTime);
            queryWrapper.ge("user_order.create_time", startTime);
        }
        if (!StringUtils.isEmpty(toSort)) {
            if (toSort.equals("DESC")) {
                queryWrapper.orderByDesc("user_order.delivery_date");

            } else {
                queryWrapper.orderByAsc("user_order.delivery_date");

            }

        } else {
            queryWrapper.orderByDesc("user_order.pay_time", "user_order.create_time");

        }
        if (!StringUtils.isEmpty(startDeliveryDate)) {

            LocalDateTime now = LocalDateTime.now();
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String endDeliveryDate = df.format(now);

            queryWrapper.le("user_order.delivery_date", endDeliveryDate);
            queryWrapper.ge("user_order.delivery_date", startDeliveryDate);
        }
        Boolean aBoolean = userRoleService.judgeUserIsAdmin(SecurityUtils.getUser().getId());

        IPage<UserOrder> pageScoped = service.pageScoped(aBoolean, page, queryWrapper);
        if (CollUtil.isNotEmpty(pageScoped.getRecords())) {
            List<UserOrder> records = pageScoped.getRecords();
            //服务包信息
            List<Integer> servicePackIds = records.stream().map(UserOrder::getServicePackId)
                    .collect(Collectors.toList());
            List<ServicePack> servicePacks = (List<ServicePack>) servicePackService.listByIds(servicePackIds);
            //查询服务包图片信息
            if (!CollectionUtils.isEmpty(servicePacks)) {
                List<ServicePackProductPic> servicePackProductPics = servicePackProductPicService.list(new QueryWrapper<ServicePackProductPic>().lambda()
                        .in(ServicePackProductPic::getServicePackId, servicePackIds));
                if (!CollectionUtils.isEmpty(servicePackProductPics)) {
                    Map<Integer, List<ServicePackProductPic>> servicePackProductPicMap = servicePackProductPics.stream()
                            .collect(Collectors.groupingBy(ServicePackProductPic::getServicePackId));
                    for (ServicePack servicePack : servicePacks) {
                        List<ServicePackProductPic> servicePackProductPics1 = servicePackProductPicMap.get(servicePack.getId());
                        if (!CollectionUtils.isEmpty(servicePackProductPics1)) {
                            servicePack.setServicePackProductPics(servicePackProductPics1);
                        } else {
                            servicePack.setServicePackProductPics(new ArrayList<>());
                        }

                    }
                }
            }
            Map<Integer, ServicePack> servicePackMap = servicePacks.stream()
                    .collect(Collectors.toMap(ServicePack::getId, t -> t));


            //查询续租订单
            List<String> userOrderNos = records.stream().map(UserOrder::getOrderNo)
                    .collect(Collectors.toList());
            List<RentRuleOrder> rentRuleOrderList = rentRuleOrderService.list(new QueryWrapper<RentRuleOrder>().lambda().in(RentRuleOrder::getUserOrderNo, userOrderNos));
            Map<String, List<RentRuleOrder>> rentRuleOrderMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(rentRuleOrderList)) {
                rentRuleOrderMap = rentRuleOrderList.stream()
                        .collect(Collectors.groupingBy(RentRuleOrder::getUserOrderNo));
            }
            for (UserOrder userOrder : records) {
                //回收时间
                if (!userOrder.getStatus().equals(5)) {
                    //计算使用天数
                    Date deliveryTime = userOrder.getDeliveryTime();
                    if (deliveryTime != null) {
                        LocalDateTime now = LocalDateTime.now();
                        ZoneId zoneId = ZoneId.systemDefault();
                        LocalDateTime deliveryTimeLo = deliveryTime.toInstant().atZone(zoneId).toLocalDateTime();
                        java.time.Duration duration = java.time.Duration.between(deliveryTimeLo, now);
                        Long l = duration.toDays();
                        userOrder.setUseDay(l.intValue());
                    }
                }

                ServicePack servicePack = servicePackMap.get(userOrder.getServicePackId());
                if (servicePack == null) {
                    servicePack = new ServicePack();
                }
                List<ServicePackProductPic> servicePackProductPics = servicePack.getServicePackProductPics();
                if (CollectionUtils.isEmpty(servicePackProductPics)) {
                    servicePack.setServicePackProductPics(new ArrayList<>());
                }
                userOrder.setServicePack(servicePack);
                //重新组装订单号
                String orderNo = userOrder.getOrderNo();
                userOrder.setRentRuleOrderList(rentRuleOrderMap.get(userOrder.getOrderNo()));

                LocalDateTime createTime = userOrder.getCreateTime();

                userOrder.setOrderNo("KF" + createTime.getYear() + createTime.getMonthValue() + createTime.getDayOfMonth() + "-" + orderNo);
            }
        }


        return RestResponse.ok(pageScoped);
    }

    //手动修改订单
    @PutMapping("/manage/updateOrder")
    public RestResponse updateOrderManual(@RequestBody UserOrder order) {
        service.updateById(order);
        if (order.getMoveTime() != null) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            UpdateOrderRecord updateOrderRecord = new UpdateOrderRecord();
            updateOrderRecord.setOrderId(order.getId());
            updateOrderRecord.setCreateUserId(SecurityUtils.getUser().getId());
            updateOrderRecord.setCreateTime(LocalDateTime.now());
            updateOrderRecord.setDescStr("修改运行时间为 " + formatter.format(order.getMoveTime()));
            updateOrderRecordService.save(updateOrderRecord);

        }
        if (order.getUseDay() != null) {
            UpdateOrderRecord updateOrderRecord = new UpdateOrderRecord();
            updateOrderRecord.setOrderId(order.getId());
            updateOrderRecord.setCreateUserId(SecurityUtils.getUser().getId());
            updateOrderRecord.setCreateTime(LocalDateTime.now());
            updateOrderRecord.setDescStr("修改使用天数为 " + order.getUseDay());
            updateOrderRecordService.save(updateOrderRecord);

        }
        return RestResponse.ok();
    }

    /**
     * 查询订单修改记录
     *
     * @param id
     * @return
     */
    @GetMapping("/queryUpdateOrderRecord")
    public RestResponse checkOrderBill(@RequestParam("pageSize") int pageSize, @RequestParam("pageNum") int pageNum, @RequestParam(value = "id", required = false) Integer id) {
        IPage page = new Page(pageNum, pageSize);

        LambdaQueryWrapper<UpdateOrderRecord> eq = new QueryWrapper<UpdateOrderRecord>().lambda();
        if (id != null) {
            eq.eq(UpdateOrderRecord::getOrderId, id);
        }
        IPage<UpdateOrderRecord> page1 = updateOrderRecordService.page(page, eq);

        return RestResponse.ok(page1);
    }

    /**
     * 判断订单是否可以开票
     * 10不可
     * 20可以
     *
     * @return
     */
    @GetMapping("/checkOrderBill")
    public RestResponse checkOrderBill(@RequestParam("id") Integer id) {
        UserOrder userOrder = service.getById(id);
        if (userOrder.getOrderType().equals(1)) {
            //租用
            RetrieveOrder one = retrieveOrderService.getOne(new QueryWrapper<RetrieveOrder>().lambda()
                    .eq(RetrieveOrder::getOrderId, id));
            if (one == null) {
                return RestResponse.ok("10");
            }
            if (!one.getStatus().equals(5)) {
                return RestResponse.ok("10");
            }
            LocalDateTime retrieveEndTime = one.getRetrieveEndTime();
            if (retrieveEndTime.plusDays(30).isBefore(LocalDateTime.now())) {
                return RestResponse.ok("10");
            }
        } else {
            //购买
            Integer servicePackId = userOrder.getServicePackId();
            SaleSpec saleSpec = saleSpecService.getOne(new QueryWrapper<SaleSpec>().lambda()
                    .eq(SaleSpec::getServicePackId, servicePackId)
                    .eq(SaleSpec::getName, "服务周期"));
            String saleSpecDescIdList1 = userOrder.getSaleSpecDescIdList();
            List<String> split = Arrays.asList(saleSpecDescIdList1.split(","));
            Integer rentDay = 0;
            if (saleSpec != null) {
                List<SaleSpecDesc> saleSpecDescList = saleSpecDescService.list(new QueryWrapper<SaleSpecDesc>().lambda()
                        .eq(SaleSpecDesc::getSaleSpecId, saleSpec.getId()));
                List<Integer> saleSpecDescIdList = saleSpecDescList.stream().map(SaleSpecDesc::getId)
                        .collect(Collectors.toList());
                saleSpecDescIdList.retainAll(split);
                Integer saleSpecDescId = saleSpecDescIdList.get(0);
                if (saleSpecDescId != null) {
                    for (SaleSpecDesc saleSpecDesc : saleSpecDescList) {
                        if (saleSpecDescId.equals(saleSpecDesc.getSaleSpecId())) {
                            String name = saleSpecDesc.getName();
                            String regEx = "[^0-9]";
                            Pattern p = Pattern.compile(regEx);
                            Matcher m = p.matcher(name);
                            String result = m.replaceAll("").trim();
                            rentDay = Integer.parseInt(result);
                        }
                    }

                }
                List<TbTrainUser> tbTrainUsers = planUserService.list(new QueryWrapper<TbTrainUser>().lambda().eq(TbTrainUser::getXtUserId, userOrder.getUserId()));
                if (CollectionUtils.isEmpty(tbTrainUsers)) {
                    return RestResponse.ok("10");
                }
                TbTrainUser tbTrainUser = tbTrainUsers.get(0);
                if (tbTrainUser.getFirstTrainTime() == null) {
                    return RestResponse.ok("10");
                }
                LocalDateTime payTime = tbTrainUser.getFirstTrainTime();
                LocalDateTime localDateTime = payTime.plusDays(rentDay);
                if (localDateTime.isBefore(LocalDateTime.now())) {
                    return RestResponse.ok("10");
                }
            }

        }


        return RestResponse.ok("20");
    }


    //上传发票
    @PutMapping("/uploadBillImage")
    public RestResponse uploadBillImage(@RequestBody UserOrder order) {
        service.updateById(order);
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        UserOrder byId = service.getById(order.getId());
        User userById = userService.getById(byId.getUserId());

        wxMpService.faPiaoNotice(userById.getMpOpenId(), "发票已上传", byId.getOrderNo(), byId.getPayment() + "", userById.getNickname(),
                "点击查看详情", "pages/myOrder/myOrder");

        UpdateOrderRecord updateOrderRecord = new UpdateOrderRecord();
        updateOrderRecord.setOrderId(order.getId());
        updateOrderRecord.setCreateUserId(SecurityUtils.getUser().getId());
        updateOrderRecord.setCreateTime(LocalDateTime.now());
        updateOrderRecord.setDescStr("上传发票");
        updateOrderRecordService.save(updateOrderRecord);
        return RestResponse.ok();
    }

    //deliveryCompanyCode快递公司编码
    //deliveryNumber 快递单号
    //发货
    @GetMapping("/manage/confirmDelivery")
    @Transactional
    public RestResponse confirDelivery(@RequestParam("id") int orderId,
                                       @RequestParam(value = "productSn1", required = false) String productSn1,
                                       @RequestParam(value = "productSn2", required = false) String productSn2,
                                       @RequestParam(value = "productSn3", required = false) String productSn3,
                                       @RequestParam(value = "deliveryCompanyCode") String deliveryCompanyCode, @RequestParam(value = "deliveryNumber", required = false) String deliveryNumber) {
        service.conformDelivery(orderId, deliveryCompanyCode, deliveryNumber, productSn1, productSn2, productSn3);
        UpdateOrderRecord updateOrderRecord = new UpdateOrderRecord();
        updateOrderRecord.setOrderId(orderId);
        updateOrderRecord.setCreateUserId(SecurityUtils.getUser().getId());
        updateOrderRecord.setCreateTime(LocalDateTime.now());
        updateOrderRecord.setDescStr("发货");
        updateOrderRecordService.save(updateOrderRecord);
        return RestResponse.ok();
    }

    @GetMapping("/manage/countScoped")
    public RestResponse countScoped() {
        UOrderStatuCountVo result = service.countScoped();
        return RestResponse.ok(result);
    }

    /**
     * 计算订单价格
     * servicePackId
     * saleSpecId
     * orderType
     * rentDay;//租用天数
     */
    @PostMapping("/calculatePrice")
    public RestResponse calculatePrice(@RequestBody UserOrder userOrder) {
        List<Integer> saleSpecDescIds = userOrder.getSaleSpecDescIds();
        String querySaleSpecIds = "";
        for (Integer saleSpecDescId : saleSpecDescIds) {
            querySaleSpecIds = querySaleSpecIds + saleSpecDescId;
        }
        querySaleSpecIds = querySaleSpecIds.chars()        // IntStream
                .sorted()
                .collect(StringBuilder::new,
                        StringBuilder::appendCodePoint,
                        StringBuilder::append)
                .toString();
        SaleSpecGroup saleSpecGroup = saleSpecGroupService.getOne(new QueryWrapper<SaleSpecGroup>().lambda()
                .eq(SaleSpecGroup::getQuerySaleSpecIds, querySaleSpecIds)
                .eq(SaleSpecGroup::getServicePackId, userOrder.getServicePackId()));

        return RestResponse.ok(saleSpecGroup);
    }

    /**
     * 根据订单id生成分享图片
     */
    @GetMapping("/shareOrder")
    public RestResponse shareOrder(@RequestParam("orderNo") String orderNo) {
        //生成一个图片返回
        String url = "https://pharos3.ewj100.com/index.html#/transferPage/helpPay?orderNo=" + orderNo;
        BufferedImage png = null;
        try {
            png = QrCodeUtil.orderImage(ServletUtils.getResponse().getOutputStream(), "", url, 300);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String name = "";
        //转换上传到oss
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        ImageOutputStream imOut = null;
        try {
            imOut = ImageIO.createImageOutputStream(bs);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            ImageIO.write(png, "png", imOut);
        } catch (IOException e) {
            e.printStackTrace();
        }
        InputStream inputStream = new ByteArrayInputStream(bs.toByteArray());
        try {
            OSS ossClient = UploadFileUtils.getOssClient(ossProperties);
            Random random = new Random();
            name = random.nextInt(10000) + System.currentTimeMillis() + "_YES.png";
            // 上传文件
            PutObjectResult putResult = ossClient.putObject(ossProperties.getBucket(), "poster/" + name, inputStream);

        } catch (Exception e) {
            e.printStackTrace();
        }
        //https://ewj-pharos.oss-cn-hangzhou.aliyuncs.com/avatar/1673835893578_b9f1ad25.png
        String resultStr = "https://ewj-pharos.oss-cn-hangzhou.aliyuncs.com/" + "poster/" + name;
        return RestResponse.ok(resultStr);

    }

    /**
     * 发货之前取消订单
     */
    @GetMapping("/cancelOrder")
    public RestResponse cancelOrder(@RequestParam("orderNo") String orderNo) {
        String[] split = orderNo.split("-");
        if (split.length == 1) {
            orderNo = split[0];
        } else {
            orderNo = split[1];
        }
        UserOrder userOrder = service.getOne(new QueryWrapper<UserOrder>().lambda().eq(UserOrder::getOrderNo, orderNo));
        if (!userOrder.getStatus().equals(2)) {
            return RestResponse.failed("用户未支付");
        }
        userOrder.setStatus(7);
        service.updateById(userOrder);
        //生成回收单
        RetrieveOrder retrieveOrder = new RetrieveOrder();
        retrieveOrder.setUserId(userOrder.getUserId());
        retrieveOrder.setCreateTime(new Date());
        retrieveOrder.setOrderNo(IdUtil.getSnowflake(0, 0).nextIdStr());
        retrieveOrder.setStatus(3);
        retrieveOrder.setUserOrderNo(userOrder.getOrderNo());
        retrieveOrder.setOrderId(userOrder.getId() + "");
        retrieveOrder.setDeptId(userOrder.getDeptId());

        Long day = 1L;

        retrieveOrder.setRentDay(day.intValue());
        retrieveOrderService.saveRetrieveOrder(retrieveOrder);

        UpdateOrderRecord updateOrderRecord = new UpdateOrderRecord();
        updateOrderRecord.setOrderId(userOrder.getId());
        updateOrderRecord.setCreateTime(LocalDateTime.now());
        updateOrderRecord.setDescStr("订单取消");
        updateOrderRecord.setCreateUserId(SecurityUtils.getUser().getId());
        updateOrderRecordService.save(updateOrderRecord);

        return RestResponse.ok();
    }

    /**
     * 生成订单
     */
    @PostMapping("/createOrder")
    public RestResponse createOrder(@RequestBody UserOrder userOrder) {
        Integer addressId = userOrder.getAddressId();
        Address address = addressService.getById(addressId);
        userOrder.setReceiverName(address.getAddresseeName());
        userOrder.setCity(address.getCity());
        userOrder.setProvince(address.getProvince());
        userOrder.setArea(address.getArea());
        userOrder.setReceiverPhone(address.getAddresseePhone());
        userOrder.setReceiverDetailAddress(address.getProvince() + address.getCity() + address.getArea() + address.getAddress());
        userOrder.setReceiverRegion(address.getArea());

        ServicePack byId = servicePackService.getById(userOrder.getServicePackId());
        userOrder.setDeptId(byId.getDeptId());
        userOrder.setOrderNo(IdUtil.getSnowflake(0, 0).nextIdStr());
        userOrder.setStatus(1);
        userOrder.setCreateTime(LocalDateTime.now());
        userOrder.setUserId(SecurityUtils.getUser().getId());


        List<Integer> saleSpecDescIds = userOrder.getSaleSpecDescIds();
        String querySaleSpecIds = "";
        String userOrderSaleSpecDescIds = "";
        for (Integer saleSpecDescId : saleSpecDescIds) {
            querySaleSpecIds = querySaleSpecIds + saleSpecDescId;
            userOrderSaleSpecDescIds = userOrderSaleSpecDescIds + "," + saleSpecDescId;
        }
        userOrder.setSaleSpecDescIdList(userOrderSaleSpecDescIds);
        querySaleSpecIds = querySaleSpecIds.chars()        // IntStream
                .sorted()
                .collect(StringBuilder::new,
                        StringBuilder::appendCodePoint,
                        StringBuilder::append)
                .toString();
        SaleSpecGroup saleSpecGroup = saleSpecGroupService.getOne(new QueryWrapper<SaleSpecGroup>().lambda()
                .eq(SaleSpecGroup::getQuerySaleSpecIds, querySaleSpecIds)
                .eq(SaleSpecGroup::getServicePackId, userOrder.getServicePackId()));
        List<SaleSpecDesc> saleSpecDescs = (List<SaleSpecDesc>) saleSpecDescService.listByIds(saleSpecDescIds);
        if (!CollectionUtils.isEmpty(saleSpecDescs)) {
            String saleSpecId = "";
            for (SaleSpecDesc saleSpecDesc : saleSpecDescs) {
                if (StringUtils.isEmpty(saleSpecId)) {
                    saleSpecId = saleSpecDesc.getName();
                } else {
                    saleSpecId = saleSpecId + "/" + saleSpecDesc.getName();

                }

            }
            userOrder.setSaleSpecId(saleSpecId);

        }
        userOrder.setQuerySaleSpecIds(querySaleSpecIds);
        //查询订单规格值的服务周期
        List<SaleSpec> saleSpecList = saleSpecService.list(new QueryWrapper<SaleSpec>().lambda()
                .eq(SaleSpec::getServicePackId, userOrder.getServicePackId())
                .eq(SaleSpec::getName, "服务周期"));
        if (!CollectionUtils.isEmpty(saleSpecList)) {
            SaleSpec saleSpec = saleSpecList.get(0);
            List<SaleSpecDesc> saleSpecDescList = saleSpecDescService.list(new QueryWrapper<SaleSpecDesc>().lambda()
                    .eq(SaleSpecDesc::getSaleSpecId, saleSpec.getId())
            );
            if (!CollectionUtils.isEmpty(saleSpecDescList)) {
                for (SaleSpecDesc saleSpecDesc : saleSpecDescList) {
                    if (saleSpecDescIds.contains(saleSpecDesc.getId())) {
                        String name = saleSpecDesc.getName();
                        String regEx = "[^0-9]";
                        Pattern p = Pattern.compile(regEx);
                        Matcher m = p.matcher(name);
                        String result = m.replaceAll("").trim();
                        if (!StringUtils.isEmpty(result)) {
                            userOrder.setSaleSpecServiceEndTime(Integer.parseInt(result));
                        }
                    }
                }
            }
        }
        //计算订单价格
        BigDecimal payment = new BigDecimal(saleSpecGroup.getPrice());
        userOrder.setPayment(payment);
        Integer orderType = 2;//判断是租用还是购买
        ServicePack servicePack = servicePackService.getById(userOrder.getServicePackId());
        if (servicePack.getRentBuy() != null) {
            orderType = servicePack.getRentBuy();
        } else {
            List<SaleSpecDesc> saleSpecDescList = (List<SaleSpecDesc>) saleSpecDescService.listByIds(saleSpecDescIds);
            for (SaleSpecDesc saleSpecDesc : saleSpecDescList) {
                if (saleSpecDesc.getName().equals("租赁")) {
                    orderType = 1;
                }
            }
        }
        userOrder.setOrderType(orderType);
        userOrder.setProductPic(saleSpecGroup.getUrlImage());
        service.save(userOrder);
        if (userOrder.getPayType().equals(2)) {
            return RestResponse.ok(userOrder);
        }
        //生成一个图片返回
        String url = "https://pharos3.ewj100.com/index.html#/transferPage/helpPay?orderNo=" + userOrder.getOrderNo();
        BufferedImage png = null;
        try {
            png = QrCodeUtil.orderImage(ServletUtils.getResponse().getOutputStream(), "", url, 300);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String name = "";
        //转换上传到oss
        ByteArrayOutputStream bs = new ByteArrayOutputStream();
        ImageOutputStream imOut = null;
        try {
            imOut = ImageIO.createImageOutputStream(bs);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            ImageIO.write(png, "png", imOut);
        } catch (IOException e) {
            e.printStackTrace();
        }
        InputStream inputStream = new ByteArrayInputStream(bs.toByteArray());
        try {
            OSS ossClient = UploadFileUtils.getOssClient(ossProperties);
            Random random = new Random();
            name = random.nextInt(10000) + System.currentTimeMillis() + "_YES.png";
            // 上传文件
            PutObjectResult putResult = ossClient.putObject(ossProperties.getBucket(), "poster/" + name, inputStream);

        } catch (Exception e) {
            e.printStackTrace();
        }
        //https://ewj-pharos.oss-cn-hangzhou.aliyuncs.com/avatar/1673835893578_b9f1ad25.png
        String resultStr = "https://ewj-pharos.oss-cn-hangzhou.aliyuncs.com/" + "poster/" + name;
        return RestResponse.ok(resultStr);

    }


    /**
     * 购买者申请单的增加
     *
     * @param userOrder
     * @return
     */
    @PostMapping("/user/add")
    public RestResponse addOrder(@RequestBody UserOrder userOrder) {
        Integer addressId = userOrder.getAddressId();
        Address address = addressService.getById(addressId);
        userOrder.setReceiverName(address.getAddresseeName());
        userOrder.setCity(address.getCity());
        userOrder.setProvince(address.getProvince());
        userOrder.setArea(address.getArea());
        userOrder.setReceiverPhone(address.getAddresseePhone());
        userOrder.setReceiverDetailAddress(address.getProvince() + address.getCity() + address.getArea() + address.getAddress());
        userOrder.setReceiverRegion(address.getArea());

        ServicePack byId = servicePackService.getById(userOrder.getServicePackId());
        userOrder.setDeptId(byId.getDeptId());
        userOrder.setOrderNo(IdUtil.getSnowflake(0, 0).nextIdStr());
        userOrder.setStatus(1);
        userOrder.setCreateTime(LocalDateTime.now());
        userOrder.setUserId(SecurityUtils.getUser().getId());


        List<Integer> saleSpecDescIds = userOrder.getSaleSpecDescIds();
        String querySaleSpecIds = "";
        String userOrderSaleSpecDescIds = "";
        for (Integer saleSpecDescId : saleSpecDescIds) {
            querySaleSpecIds = querySaleSpecIds + saleSpecDescId;
            userOrderSaleSpecDescIds = userOrderSaleSpecDescIds + "," + saleSpecDescId;
        }
        userOrder.setSaleSpecDescIdList(userOrderSaleSpecDescIds);
        querySaleSpecIds = querySaleSpecIds.chars()        // IntStream
                .sorted()
                .collect(StringBuilder::new,
                        StringBuilder::appendCodePoint,
                        StringBuilder::append)
                .toString();
        SaleSpecGroup saleSpecGroup = saleSpecGroupService.getOne(new QueryWrapper<SaleSpecGroup>().lambda()
                .eq(SaleSpecGroup::getQuerySaleSpecIds, querySaleSpecIds)
                .eq(SaleSpecGroup::getServicePackId, userOrder.getServicePackId()));
        List<SaleSpecDesc> saleSpecDescs = (List<SaleSpecDesc>) saleSpecDescService.listByIds(saleSpecDescIds);
        if (!CollectionUtils.isEmpty(saleSpecDescs)) {
            String saleSpecId = "";
            for (SaleSpecDesc saleSpecDesc : saleSpecDescs) {
                saleSpecId = saleSpecId + "/" + saleSpecDesc.getName();

            }
            userOrder.setSaleSpecId(saleSpecId);

        }
        userOrder.setQuerySaleSpecIds(querySaleSpecIds);
        //查询订单规格值的服务周期
        List<SaleSpec> saleSpecList = saleSpecService.list(new QueryWrapper<SaleSpec>().lambda()
                .eq(SaleSpec::getServicePackId, userOrder.getServicePackId())
                .eq(SaleSpec::getName, "服务周期"));
        if (!CollectionUtils.isEmpty(saleSpecList)) {
            SaleSpec saleSpec = saleSpecList.get(0);
            List<SaleSpecDesc> saleSpecDescList = saleSpecDescService.list(new QueryWrapper<SaleSpecDesc>().lambda()
                    .eq(SaleSpecDesc::getSaleSpecId, saleSpec.getId())
            );
            if (!CollectionUtils.isEmpty(saleSpecDescList)) {
                for (SaleSpecDesc saleSpecDesc : saleSpecDescList) {
                    if (saleSpecDescIds.contains(saleSpecDesc.getId())) {
                        String name = saleSpecDesc.getName();
                        String regEx = "[^0-9]";
                        Pattern p = Pattern.compile(regEx);
                        Matcher m = p.matcher(name);
                        String result = m.replaceAll("").trim();
                        if (!StringUtils.isEmpty(result)) {
                            userOrder.setSaleSpecServiceEndTime(Integer.parseInt(result));
                        }
                    }
                }
            }
        }
        //计算订单价格
        BigDecimal payment = new BigDecimal(saleSpecGroup.getPrice());
        userOrder.setPayment(payment);
        userOrder.setSaleSpecRecoveryPrice(saleSpecGroup.getRecoveryPrice());
        Integer orderType = 2;
        ServicePack servicePack = servicePackService.getById(userOrder.getServicePackId());


        if (servicePack.getRentBuy() != null) {
            orderType = servicePack.getRentBuy();
        } else {
            List<SaleSpecDesc> saleSpecDescList = (List<SaleSpecDesc>) saleSpecDescService.listByIds(saleSpecDescIds);
            for (SaleSpecDesc saleSpecDesc : saleSpecDescList) {
                if (saleSpecDesc.getName().equals("租赁")) {
                    orderType = 1;
                }
            }
        }
        userOrder.setOrderType(orderType);
        userOrder.setProductPic(saleSpecGroup.getUrlImage());
        service.save(userOrder);

        RestResponse restResponse = wxPayFarosService.unifiedOrder(userOrder.getOrderNo(), null);
        return restResponse;
    }

    /**
     * 用户查询自己的订单
     */
    @GetMapping("/user/listMy")
    public RestResponse listMy() {
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());
        Page<UserOrder> page = getPage();
        queryWrapper.eq("user_id", SecurityUtils.getUser().getId());
        IPage iPage = service.listMyOrder(page, queryWrapper);
        if (CollUtil.isNotEmpty(iPage.getRecords())) {
            List<UserOrder> records = iPage.getRecords();
            //服务包信息
            //服务包信息
            List<Integer> servicePackIds = records.stream().map(UserOrder::getServicePackId)
                    .collect(Collectors.toList());
            List<Integer> userServicePackInfoIds = records.stream().map(UserOrder::getUserServicePackageInfoId)
                    .collect(Collectors.toList());
            Map<Integer, UserServicePackageInfo> userServicePackInfoMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(userServicePackInfoIds)) {
                List<UserServicePackageInfo> userServicePackageInfos = (List<UserServicePackageInfo>) userServicePackageInfoService.listByIds(userServicePackInfoIds);
                if (!CollectionUtils.isEmpty(userServicePackageInfos)) {
                    userServicePackInfoMap = userServicePackageInfos.stream()
                            .collect(Collectors.toMap(UserServicePackageInfo::getId, t -> t));


                }
            }
            List<ServicePack> servicePacks = (List<ServicePack>) servicePackService.listByIds(servicePackIds);
            //查询服务包图片信息
            if (!CollectionUtils.isEmpty(servicePacks)) {
                List<ServicePackProductPic> servicePackProductPics = servicePackProductPicService.list(new QueryWrapper<ServicePackProductPic>().lambda()
                        .in(ServicePackProductPic::getServicePackId, servicePackIds));
                if (!CollectionUtils.isEmpty(servicePackProductPics)) {
                    Map<Integer, List<ServicePackProductPic>> servicePackProductPicMap = servicePackProductPics.stream()
                            .collect(Collectors.groupingBy(ServicePackProductPic::getServicePackId));
                    for (ServicePack servicePack : servicePacks) {
                        servicePack.setServicePackProductPics(servicePackProductPicMap.get(servicePack.getId()));
                    }
                }
            }
            Map<Integer, ServicePack> servicePackMap = servicePacks.stream()
                    .collect(Collectors.toMap(ServicePack::getId, t -> t));

            //赠送的服务信息
            List<ServicePackageInfo> servicePackageInfos = servicePackageInfoService.list(new QueryWrapper<ServicePackageInfo>().lambda()
                    .in(ServicePackageInfo::getServicePackageId, servicePackIds));
            Map<Integer, List<ServicePackageInfo>> servicePackageInfoMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(servicePackageInfos)) {
                servicePackageInfoMap = servicePackageInfos.stream()
                        .collect(Collectors.groupingBy(ServicePackageInfo::getServicePackageId));
            }
            //查询续租订单
            List<String> userOrderNos = records.stream().map(UserOrder::getOrderNo)
                    .collect(Collectors.toList());
            List<RentRuleOrder> rentRuleOrderList = rentRuleOrderService.list(new QueryWrapper<RentRuleOrder>().lambda().in(RentRuleOrder::getUserOrderNo, userOrderNos));
            Map<String, List<RentRuleOrder>> rentRuleOrderMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(rentRuleOrderList)) {
                rentRuleOrderMap = rentRuleOrderList.stream()
                        .collect(Collectors.groupingBy(RentRuleOrder::getUserOrderNo));
            }
            for (UserOrder userOrder : records) {
                UserServicePackageInfo userServicePackageInfo = userServicePackInfoMap.get(userOrder.getUserServicePackageInfoId());
                userOrder.setUserServicePackageInfo(userServicePackageInfo);
                userOrder.setServicePack(servicePackMap.get(userOrder.getServicePackId()));
                List<ServicePackageInfo> servicePackageInfos1 = servicePackageInfoMap.get(userOrder.getServicePackId());
                userOrder.setServicePackageInfos(servicePackageInfos1);
                //重新组装订单号
                String orderNo = userOrder.getOrderNo();
                List<RentRuleOrder> rentRuleOrderList1 = rentRuleOrderMap.get(userOrder.getOrderNo());

                userOrder.setRentRuleOrderList(rentRuleOrderList1);

                LocalDateTime createTime = userOrder.getCreateTime();

                userOrder.setOrderNo("KF" + createTime.getYear() + createTime.getMonthValue() + createTime.getDayOfMonth() + "-" + orderNo);
            }
        }
        return RestResponse.ok(iPage);
    }

    /**
     * 用户查询自己的订单数量
     */
    @GetMapping("/user/listMyStateCount")
    public RestResponse listMyStateCount() {
        MyStateCount myStateCount = new MyStateCount();
        myStateCount.setPendingPayment(service.count(Wrappers.<UserOrder>lambdaQuery()
                .eq(UserOrder::getUserId, SecurityUtils.getUser().getId())
                .eq(UserOrder::getStatus, 1)));//待付款
        myStateCount.setPendingDelivery(service.count(Wrappers.<UserOrder>lambdaQuery()
                .eq(UserOrder::getUserId, SecurityUtils.getUser().getId())
                .eq(UserOrder::getStatus, 2))); //待发货
        myStateCount.setPendingReward(service.count(Wrappers.<UserOrder>lambdaQuery()
                .eq(UserOrder::getUserId, SecurityUtils.getUser().getId())
                .eq(UserOrder::getStatus, 3)));//待收货
        myStateCount.setUsedCount(service.count(Wrappers.<UserOrder>lambdaQuery()
                .eq(UserOrder::getUserId, SecurityUtils.getUser().getId())
                .eq(UserOrder::getStatus, 4)));//使用中
        myStateCount.setPendingRecycle(service.count(Wrappers.<UserOrder>lambdaQuery()
                .eq(UserOrder::getUserId, SecurityUtils.getUser().getId())
                .eq(UserOrder::getStatus, 4)));//待回收
        return RestResponse.ok(myStateCount);
    }


    /**
     * 用户查询自己的订单详细信息
     */
    @GetMapping("/user/orderDetail")
    public RestResponse getMyOrderDetail(@RequestParam("orderId") int orderId) {
        UserOrder userOrder = service.getById(orderId);
        //查询续租订单

        List<RentRuleOrder> rentRuleOrderList = rentRuleOrderService.list(new QueryWrapper<RentRuleOrder>().lambda().eq(RentRuleOrder::getUserOrderNo, userOrder.getOrderNo()));
        userOrder.setRentRuleOrderList(rentRuleOrderList);

        //就诊人
        Integer patientUserId = userOrder.getPatientUserId();
        userOrder.setPatientUser(patientUserService.getById(patientUserId));
        //表单
        List<FormUserData> list = formUserDataService.list(new QueryWrapper<FormUserData>().lambda()
                .eq(FormUserData::getOrderId, userOrder.getId()));
        if (!CollectionUtils.isEmpty(list)) {
            userOrder.setIsForm(1);
        }
        //服务信息
        List<ServicePackageInfo> servicePackageInfo = servicePackageInfoService.list(new QueryWrapper<ServicePackageInfo>().lambda()
                .in(ServicePackageInfo::getServicePackageId, userOrder.getServicePackId()));
        userOrder.setServicePackageInfos(servicePackageInfo);
        //服务包信息
        ServicePack servicePack = servicePackService.getById(userOrder.getServicePackId());
        //查询服务包图片信息
        if (servicePack != null) {
            List<ServicePackProductPic> servicePackProductPics = servicePackProductPicService.list(new QueryWrapper<ServicePackProductPic>().lambda()
                    .eq(ServicePackProductPic::getServicePackId, servicePack.getId()));
            if (!CollectionUtils.isEmpty(servicePackProductPics)) {

                servicePack.setServicePackProductPics(servicePackProductPics);

            }
        }
        userOrder.setServicePack(servicePack);
        userOrder.setDoctorTeamName(doctorTeamService.getById(userOrder.getDoctorTeamId()).getName());
        //重新组装订单号
        String orderNo = userOrder.getOrderNo();
        LocalDateTime createTime = userOrder.getCreateTime();

        userOrder.setOrderNo("KF" + createTime.getYear() + createTime.getMonthValue() + createTime.getDayOfMonth() + "-" + orderNo);
        List<SaleSpecGroup> saleSpecGroupList = saleSpecGroupService.list(new QueryWrapper<SaleSpecGroup>().lambda().eq(SaleSpecGroup::getQuerySaleSpecIds, userOrder.getQuerySaleSpecIds())
                .eq(SaleSpecGroup::getServicePackId, userOrder.getServicePackId()));
        if (!CollectionUtils.isEmpty(saleSpecGroupList)) {
            SaleSpecGroup saleSpecGroup = saleSpecGroupList.get(0);
            userOrder.setServiceCount(saleSpecGroup.getServiceCount());
        }
        UserServicePackageInfo byId = userServicePackageInfoService.getById(userOrder.getUserServicePackageInfoId());
        userOrder.setUserServicePackageInfo(byId);

        return RestResponse.ok(userOrder);
    }

    public static void main(String[] args) {
        Integer status = 1;
        if (status >= 1 && status <= 2) {
            System.out.println(status);
        }

    }

    /**
     * 查询订单详细信息
     */
    @GetMapping("/user/orderDetailByOrderNo")
    public RestResponse orderDetailByOrderNo(@RequestParam("orderNo") String orderNo) {
        String[] split = orderNo.split("-");
        if (split.length == 1) {
            orderNo = split[0];
        } else {
            orderNo = split[1];
        }

        UserOrder userOrder = service.getOne(new QueryWrapper<UserOrder>().lambda()
                .eq(UserOrder::getOrderNo, orderNo));
        //就诊人
        Integer patientUserId = userOrder.getPatientUserId();
        userOrder.setPatientUser(patientUserService.getById(patientUserId));
        //表单
        List<FormUserData> list = formUserDataService.list(new QueryWrapper<FormUserData>().lambda()
                .eq(FormUserData::getOrderId, userOrder.getId()));
        if (!CollectionUtils.isEmpty(list)) {
            userOrder.setIsForm(1);
        }
        //服务信息
        List<ServicePackageInfo> servicePackageInfo = servicePackageInfoService.list(new QueryWrapper<ServicePackageInfo>().lambda()
                .in(ServicePackageInfo::getServicePackageId, userOrder.getServicePackId()));
        userOrder.setServicePackageInfos(servicePackageInfo);
        //服务包信息
        ServicePack servicePack = servicePackService.getById(userOrder.getServicePackId());
        //查询服务包医院
        Integer hospitalId = servicePack.getHospitalId();
        if (hospitalId != null) {
            HospitalInfo byId = hospitalInfoService.getById(hospitalId);
            if (byId != null) {
                servicePack.setHospitalName(byId.getName());
            }
        }
        //查询服务包图片信息
        if (servicePack != null) {
            List<ServicePackProductPic> servicePackProductPics = servicePackProductPicService.list(new QueryWrapper<ServicePackProductPic>().lambda()
                    .eq(ServicePackProductPic::getServicePackId, servicePack.getId()));
            if (!CollectionUtils.isEmpty(servicePackProductPics)) {

                servicePack.setServicePackProductPics(servicePackProductPics);

            }
        }
        userOrder.setServicePack(servicePack);
        userOrder.setDoctorTeamName(doctorTeamService.getById(userOrder.getDoctorTeamId()).getName());

        return RestResponse.ok(userOrder);
    }

    @GetMapping("/getById")
    public RestResponse getById(@RequestParam String id) {
        UserOrder userOrder = service.getById(id);
        //查询选择的规格组
        List<SaleSpecGroup> list = saleSpecGroupService.list(new QueryWrapper<SaleSpecGroup>().lambda()
                .eq(SaleSpecGroup::getQuerySaleSpecIds, userOrder.getQuerySaleSpecIds())
                .eq(SaleSpecGroup::getServicePackId, userOrder.getServicePackId()));
        if (!CollectionUtils.isEmpty(list)) {
            userOrder.setSaleSpecGroup(list.get(0));
        }
        return RestResponse.ok(userOrder);
    }

    /**
     * 确认收货
     *
     * @return
     */
    @GetMapping("/confirmReceieve")
    public RestResponse confirmReceieve(@RequestParam Integer id) {
        UserOrder userOrder = service.getById(id);
        userOrder.setStatus(4);
        userOrder.setRevTime(LocalDateTime.now());
        service.updateById(userOrder);
        return RestResponse.ok(userOrder);
    }

    /**
     * excel导出发货模版
     *
     * @return
     */
    @GetMapping("/deliveryMoBan")
    public RestResponse deliveryMoBan(HttpServletResponse response) {
        String cFileName = null;
        try {
            cFileName = URLEncoder.encode("DeliveryMoBan", "UTF-8");
            List<DeliveryMoBan> deliveryMoBans = new ArrayList<>();
            ExcelUtil.writeDeliveryMoBanExcel(response, deliveryMoBans, cFileName, "DeliveryMoBan", DeliveryMoBan.class);

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return RestResponse.ok();
    }

    /**
     * excel导入发货模版
     *
     * @return
     */
    @PostMapping("/deliveryMoBanImport")
    public RestResponse deliveryMoBanImport(@RequestPart(value = "file") MultipartFile file) {
        try {
            List<DeliveryMoBan> deliveryMoBans = EasyExcel.read(file.getInputStream())
                    .head(DeliveryMoBan.class)
                    .sheet()
                    .doReadSync();
            if (!CollectionUtils.isEmpty(deliveryMoBans)) {
                Map<String, DeliveryMoBan> deliveryMoBanmap = new HashMap<>();
                for (DeliveryMoBan deliveryMoBan : deliveryMoBans) {
                    String orderNo = deliveryMoBan.getOrderNo();
                    String[] split = orderNo.split("-");
                    if (split.length == 1) {
                        orderNo = split[0];
                    } else {
                        orderNo = split[1];
                    }
                    DeliveryMoBan deliveryMoBan1 = deliveryMoBanmap.get(orderNo);
                    if (deliveryMoBan1 == null) {
                        deliveryMoBanmap.put(orderNo, deliveryMoBan);
                    }
                }
                List<String> orderNos = deliveryMoBans.stream().map(DeliveryMoBan::getOrderNo)
                        .collect(Collectors.toList());
                List<String> orderNos1 = new ArrayList<>();
                for (String orderNo : orderNos) {
                    String[] split = orderNo.split("-");
                    if (split.length == 1) {
                        orderNo = split[0];
                    } else {
                        orderNo = split[1];
                    }
                    orderNos1.add(orderNo);
                }
                List<UserOrder> userOrders = service.list(new QueryWrapper<UserOrder>().lambda()
                        .in(UserOrder::getOrderNo, orderNos1)
                        .eq(UserOrder::getStatus, 2));
                if (!CollectionUtils.isEmpty(userOrders)) {

                    for (UserOrder userOrder : userOrders) {
                        DeliveryMoBan deliveryMoBan = deliveryMoBanmap.get(userOrder.getOrderNo());
                        String deliveryCompanyCode = "";
                        switch (deliveryMoBan.getName()) {
                            case "京东":
                                deliveryCompanyCode = "jd";
                                break; //可选
                            case "德邦":
                                deliveryCompanyCode = "debangkuaidi";
                                break; //可选
                            case "顺丰":
                                deliveryCompanyCode = "shunfeng";
                                break; //可选
                            case "极兔":
                                deliveryCompanyCode = "jtexpress";
                                break; //可选
                            case "圆通":
                                deliveryCompanyCode = "yuantong";
                                break; //可选
                            case "申通":
                                deliveryCompanyCode = "shentong";
                                break; //可选
                            case "中通":
                                deliveryCompanyCode = "zhongtong";
                                break; //可选
                            case "韵达":
                                deliveryCompanyCode = "yunda";
                                break; //可选
                            case "邮政":
                                deliveryCompanyCode = "youzhengguonei";
                                break; //可选
                            case "百世":
                                deliveryCompanyCode = "huitongkuaidi";
                                break; //可选
                        }

                        userOrder.setStatus(3);
                        userOrder.setDeliveryCompanyCode(deliveryCompanyCode);
                        userOrder.setDeliverySn(deliveryMoBan.getDeliverySn());
                        userOrder.setDeliveryNumber(deliveryMoBan.getDeliverySn());
                        userOrder.setDeliveryTime(new Date());
                        userOrder.setProductSn1(deliveryMoBan.getProduct_sn1());
                        userOrder.setProductSn2(deliveryMoBan.getProduct_sn2());
                        userOrder.setProductSn3(deliveryMoBan.getProduct_sn3());
                        if (!StringUtils.isEmpty(deliveryMoBan.getProduct_sn1())) {
                            Integer userId = userOrder.getUserId();
                            TbTrainUser infoByUXtUserId = planUserService.getInfoByUXtUserId(userId);
                            if (infoByUXtUserId != null) {
                                String userId1 = infoByUXtUserId.getUserId();
                                List<ProductStock> list = productStockService.list(new QueryWrapper<ProductStock>().lambda()
                                        .eq(ProductStock::getProductSn, deliveryMoBan.getProduct_sn1())
                                        .eq(ProductStock::getDel, 1));
                                if (!CollectionUtils.isEmpty(list)) {
                                    ProductStock productStock = list.get(0);
                                    boolean remove = deviceScanSignLogService.remove(new QueryWrapper<DeviceScanSignLog>().lambda().eq(DeviceScanSignLog::getMacAddress, productStock.getMacAddress()));
                                    DeviceScanSignLog deviceScanSignLog = new DeviceScanSignLog();
                                    deviceScanSignLog.setMacAddress(productStock.getMacAddress());
                                    deviceScanSignLog.setUserId(userId1);
                                    deviceScanSignLog.setCreateTime(new Date());
                                    deviceScanSignLogService.save(deviceScanSignLog);
                                }

                            }
                        }
                    }
                    service.updateBatchById(userOrders);
                }


            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        return RestResponse.ok();
    }

    /**
     * 订单导出excel
     *
     * @return
     */
    @GetMapping("/exportOrder")
    public RestResponse exportOrder(HttpServletResponse response, @RequestParam(value = "servicePackName", required = false) String servicePackName,
                                    @RequestParam(value = "startTime", required = false) String startTime,
                                    @RequestParam(value = "endTime", required = false) String endTime,
                                    @RequestParam(value = "nickname", required = false) String nickname,
                                    @RequestParam(value = "receiverPhone", required = false) String receiverPhone,
                                    @RequestParam(value = "userId", required = false) Integer userId,
                                    @RequestParam(value = "orderType", required = false) String orderType,
                                    @RequestParam(value = "status", required = false) String orderStatus) {
        User user = userService.getById(userId);
        QueryWrapper queryWrapper = new QueryWrapper<>();
        if (!StringUtils.isEmpty(orderStatus) && !orderStatus.equals("null")) {
            queryWrapper.eq("user_order.status", Integer.parseInt(orderStatus));
        }
        if (!StringUtils.isEmpty(orderType) && !orderType.equals("null")) {
            queryWrapper.eq("user_order.order_type", Integer.parseInt(orderType));
        }
        if (!StringUtils.isEmpty(servicePackName) && !servicePackName.equals("null")) {
            queryWrapper.eq("service_pack.name", servicePackName);
        }
        if (!StringUtils.isEmpty(nickname) && !nickname.equals("null")) {
            queryWrapper.eq("patient_user.name", nickname);
        }
        if (!StringUtils.isEmpty(receiverPhone) && !receiverPhone.equals("null")) {
            queryWrapper.eq("user_order.receiver_phone", receiverPhone);
        }
        if (!StringUtils.isEmpty(startTime) && !startTime.equals("null")) {
            if (StringUtils.isEmpty(endTime)) {
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                endTime = df.format(now);
            }
            queryWrapper.le("user_order.create_time", endTime);
            queryWrapper.ge("user_order.create_time", startTime);
        }
        Boolean aBoolean = userRoleService.judgeUserIsAdmin(userId);
        if (!aBoolean) {
            queryWrapper.eq("user_order.dept_id", user.getDeptId());
        }
        List<UserOrder> userOrders = service.scoped(queryWrapper);
        if (!CollectionUtils.isEmpty(userOrders)) {
            //服务包信息
            List<Integer> servicePackIds = userOrders.stream().map(UserOrder::getServicePackId)
                    .collect(Collectors.toList());
            List<ServicePack> servicePacks = (List<ServicePack>) servicePackService.listByIds(servicePackIds);

            Map<Integer, ServicePack> servicePackMap = servicePacks.stream()
                    .collect(Collectors.toMap(ServicePack::getId, t -> t));
            for (UserOrder userOrder : userOrders) {
                userOrder.setServicePack(servicePackMap.get(userOrder.getServicePackId()));
                String orderNo = userOrder.getOrderNo();
                LocalDateTime createTime = userOrder.getCreateTime();

                userOrder.setOrderNo("KF" + createTime.getYear() + createTime.getMonthValue() + createTime.getDayOfMonth() + "-" + orderNo);

            }


            String cFileName = null;
            try {
                DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                DateTimeFormatter df1 = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                cFileName = URLEncoder.encode("order", "UTF-8");
                List<UserOrderExcel> userOrderExcels = new ArrayList<>();
                for (UserOrder userOrder : userOrders) {
                    UserOrderExcel userOrderExcel = new UserOrderExcel();
                    userOrderExcel.setProductSn1(userOrder.getProductSn1());
                    userOrderExcel.setProductSn2(userOrder.getProductSn2());
                    userOrderExcel.setProductSn3(userOrder.getProductSn3());

                    //回收时间
                    if (!userOrder.getStatus().equals(5)) {
                        //计算使用天数
                        Date deliveryTime = userOrder.getDeliveryTime();
                        if (deliveryTime != null) {
                            LocalDateTime now = LocalDateTime.now();
                            ZoneId zoneId = ZoneId.systemDefault();
                            LocalDateTime deliveryTimeLo = deliveryTime.toInstant().atZone(zoneId).toLocalDateTime();
                            java.time.Duration duration = java.time.Duration.between(deliveryTimeLo, now);
                            Long l = duration.toDays();
                            userOrder.setUseDay(l.intValue());
                        }
                    }
                    userOrderExcel.setUseDay(userOrder.getUseDay() + "");
                    if (userOrder.getActualRetrieveAmount() != null) {
                        userOrderExcel.setActualRetrieveAmount(userOrder.getActualRetrieveAmount() + "");
                    } else {
                        userOrderExcel.setActualRetrieveAmount("");

                    }
                    userOrderExcel.setPhone(userOrder.getReceiverPhone());
                    userOrderExcel.setOrderNo(userOrder.getOrderNo());
                    userOrderExcel.setUserName(userOrder.getPatientUserName());
                    userOrderExcel.setPatientUserName(userOrder.getPatientUserName());
                    userOrderExcel.setPayment(userOrder.getPayment().toString());
                    userOrderExcel.setPatientUserIdCard(userOrder.getPatientUserIdCard());
                    userOrderExcel.setDoctorTeamName(userOrder.getDoctorTeamName());
                    userOrderExcel.setSpec(userOrder.getSaleSpecId());
                    if (userOrder.getPayTime() != null) {
                        userOrderExcel.setPayTime(df.format(userOrder.getPayTime()));

                    } else {
                        userOrderExcel.setPayTime("");

                    }
                    userOrderExcel.setHospitalName(userOrder.getHospitalName());
                    if (userOrder.getDeliveryDate() != null) {
                        log.info(userOrder.getDeliveryDate() + "");
                        userOrderExcel.setDeliveryDate(df1.format(userOrder.getDeliveryDate()));
                    } else {
                        userOrderExcel.setDeliveryDate("");

                    }

                    userOrderExcel.setReceiverName(userOrder.getReceiverName());
                    userOrderExcel.setReceiverDetailAddress(userOrder.getReceiverDetailAddress());
                    String status = "";
                    switch (userOrder.getStatus()) {
                        case 1:
                            status = "待付款";
                            break; //可选
                        case 2:
                            status = "待发货";
                            break; //可选
                        case 3:
                            status = "待收货";
                            break; //可选
                        case 4:
                            status = "已收货";
                            break; //可选
                        case 5:
                            status = "已回收";
                            break; //可选
                    }
                    userOrderExcel.setStatus(status);
                    if (userOrder.getServicePack() != null) {
                        userOrderExcel.setServicePackName(userOrder.getServicePack().getName());

                    } else {
                        userOrderExcel.setServicePackName("");

                    }
                    userOrderExcel.setCreateTime(df.format(userOrder.getCreateTime()));
                    userOrderExcels.add(userOrderExcel);
                }

                ExcelUtil.writeUserOrderExcel(response, userOrderExcels, cFileName, "order", UserOrderExcel.class);

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
        return RestResponse.ok();
    }


    @Override
    protected Class<UserOrder> getEntityClass() {
        return UserOrder.class;
    }

}
