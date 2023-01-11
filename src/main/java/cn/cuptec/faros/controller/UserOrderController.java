package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.dto.CalculatePriceResult;
import cn.cuptec.faros.dto.MyStateCount;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.service.*;
import cn.cuptec.faros.util.ExcelUtil;
import cn.cuptec.faros.vo.UOrderStatuCountVo;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/purchase/order")
public class UserOrderController extends AbstractBaseController<UserOrdertService, UserOrder> {
    @Resource
    private SaleSpecService saleSpecService;//销售规格

    @Resource
    private PatientUserService patientUserService;
    @Resource
    private FormUserDataService formUserDataService;
    @Resource
    private UserServicePackageInfoService userServicePackageInfoService;
    @Resource
    private ServicePackageInfoService servicePackageInfoService;
    @Resource
    private ServicePackService servicePackService;
    @Resource
    private ServicePackProductPicService servicePackProductPicService;
    @Resource
    private WxPayFarosService wxPayFarosService;
    @Resource
    private AddressService addressService;
    @Resource
    private UserService userService;
    @Resource
    private SaleSpecGroupService saleSpecGroupService;

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
                                   @RequestParam(value = "receiverPhone", required = false) String receiverPhone) {
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
        if (!StringUtils.isEmpty(startTime)) {
            if (StringUtils.isEmpty(endTime)) {
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                endTime = df.format(now);
            }
            queryWrapper.le("user_order.create_time", endTime);
            queryWrapper.ge("user_order.create_time", startTime);
        }
        IPage<UserOrder> pageScoped = service.pageScoped(page, queryWrapper);
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
                        servicePack.setServicePackProductPics(servicePackProductPicMap.get(servicePack.getId()));
                    }
                }
            }
            Map<Integer, ServicePack> servicePackMap = servicePacks.stream()
                    .collect(Collectors.toMap(ServicePack::getId, t -> t));

            for (UserOrder userOrder : records) {
                userOrder.setServicePack(servicePackMap.get(userOrder.getServicePackId()));
            }
        }


        return RestResponse.ok(pageScoped);
    }

    //手动修改订单
    @PutMapping("/manage/updateOrder")
    public RestResponse updateOrderManual(@RequestBody UserOrder order) {
        service.updateById(order);
        return RestResponse.ok();
    }

    //deliveryCompanyCode快递公司编码
    //deliveryNumber 快递单号
    //发货
    @GetMapping("/manage/confirmDelivery")
    @Transactional
    public RestResponse confirDelivery(@RequestParam("id") int orderId,
                                       @RequestParam(value = "deliveryCompanyCode") String deliveryCompanyCode, @RequestParam(value = "deliveryNumber", required = false) String deliveryNumber) {
        service.conformDelivery(orderId, deliveryCompanyCode, deliveryNumber);

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
                .eq(SaleSpecGroup::getQuerySaleSpecIds, querySaleSpecIds));

        return RestResponse.ok(saleSpecGroup);
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
        userOrder.setReceiverDetailAddress(address.getAddress());
        userOrder.setReceiverRegion(address.getArea());
        if (userOrder.getOrderType() == null) {
            userOrder.setOrderType(1);
        }
        ServicePack byId = servicePackService.getById(userOrder.getServicePackId());
        userOrder.setDeptId(byId.getDeptId());
        userOrder.setOrderNo(IdUtil.getSnowflake(0, 0).nextIdStr());
        userOrder.setStatus(1);
        userOrder.setCreateTime(LocalDateTime.now());
        userOrder.setUserId(SecurityUtils.getUser().getId());


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
                .eq(SaleSpecGroup::getQuerySaleSpecIds, querySaleSpecIds));
        //计算订单价格
        BigDecimal payment = new BigDecimal(saleSpecGroup.getPrice());
        userOrder.setPayment(payment);
        service.save(userOrder);

        RestResponse restResponse = wxPayFarosService.unifiedOrder(userOrder.getOrderNo());
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
            for (UserOrder userOrder : records) {
                userOrder.setServicePack(servicePackMap.get(userOrder.getServicePackId()));
                List<ServicePackageInfo> servicePackageInfos1 = servicePackageInfoMap.get(userOrder.getServicePackId());
                userOrder.setServicePackageInfos(servicePackageInfos1);
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
        return RestResponse.ok(userOrder);
    }

    @GetMapping("/getById")
    public RestResponse getById(@RequestParam String id) {
        UserOrder userOrder = service.getById(id);

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
                    DeliveryMoBan deliveryMoBan1 = deliveryMoBanmap.get(deliveryMoBan.getOrderNo());
                    if (deliveryMoBan1 == null) {
                        deliveryMoBanmap.put(deliveryMoBan.getOrderNo(), deliveryMoBan);
                    }
                }
                List<String> orderNos = deliveryMoBans.stream().map(DeliveryMoBan::getOrderNo)
                        .collect(Collectors.toList());
                List<UserOrder> userOrders = service.list(new QueryWrapper<UserOrder>().lambda()
                        .in(UserOrder::getOrderNo, orderNos)
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
        if (!StringUtils.isEmpty(startTime)) {
            if (StringUtils.isEmpty(endTime)) {
                LocalDateTime now = LocalDateTime.now();
                DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                endTime = df.format(now);
            }
            queryWrapper.le("user_order.create_time", endTime);
            queryWrapper.ge("user_order.create_time", startTime);
        }
        queryWrapper.eq("user_order.dept_id", user.getDeptId());
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
            }


            String cFileName = null;
            try {
                DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                cFileName = URLEncoder.encode("order", "UTF-8");
                List<UserOrderExcel> userOrderExcels = new ArrayList<>();
                for (UserOrder userOrder : userOrders) {
                    UserOrderExcel userOrderExcel = new UserOrderExcel();
                    userOrderExcel.setOrderNo(userOrder.getOrderNo());
                    userOrderExcel.setUserName(userOrder.getPatientUserName());
                    userOrderExcel.setPayment(userOrder.getPayment().toString());
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
                    userOrderExcel.setServicePackName(userOrder.getServicePack().getName());
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
