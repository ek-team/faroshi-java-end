package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.dto.CalculatePriceResult;
import cn.cuptec.faros.dto.MyStateCount;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.service.*;
import cn.cuptec.faros.vo.UOrderStatuCountVo;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
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
            //规格信息
            List<String> saleSpecIds = records.stream().map(UserOrder::getSaleSpecId)
                    .collect(Collectors.toList());
            List<SaleSpec> saleSpecs = (List<SaleSpec>) saleSpecService.listByIds(saleSpecIds);
            Map<Integer, SaleSpec> saleSpecMap = saleSpecs.stream()
                    .collect(Collectors.toMap(SaleSpec::getId, t -> t));
            for (UserOrder userOrder : records) {
                userOrder.setServicePack(servicePackMap.get(userOrder.getServicePackId()));
                userOrder.setSaleSpec(saleSpecMap.get(userOrder.getSaleSpecId()));
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
        SaleSpec saleSpec = saleSpecService.getById(userOrder.getSaleSpecId());
        if (userOrder.getOrderType() != null && userOrder.getOrderType().equals(2)) {
            //购买


            CalculatePriceResult result = new CalculatePriceResult();
            result.setTotalAmount(new BigDecimal(saleSpec.getRent()));
            return RestResponse.ok(result);
        }

        CalculatePriceResult result = new CalculatePriceResult();
        result.setTotalAmount(new BigDecimal(saleSpec.getRent()));//总价
        result.setRecoveryPrice(saleSpec.getRecoveryPrice());
        return RestResponse.ok(result);
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

        //计算订单价格
        BigDecimal payment = null;
        SaleSpec saleSpec = saleSpecService.getById(userOrder.getSaleSpecId());
        if (userOrder.getOrderType().equals(2)) {
            //购买
            payment = new BigDecimal(saleSpec.getRent());
        } else {
            payment = new BigDecimal(saleSpec.getRent());
        }

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
            //规格信息
            List<String> saleSpecIds = records.stream().map(UserOrder::getSaleSpecId)
                    .collect(Collectors.toList());
            List<SaleSpec> saleSpecs = (List<SaleSpec>) saleSpecService.listByIds(saleSpecIds);
            Map<Integer, SaleSpec> saleSpecMap = saleSpecs.stream()
                    .collect(Collectors.toMap(SaleSpec::getId, t -> t));
            //产品规格信息
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
                userOrder.setSaleSpec(saleSpecMap.get(userOrder.getSaleSpecId()));
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


    @Override
    protected Class<UserOrder> getEntityClass() {
        return UserOrder.class;
    }

}
