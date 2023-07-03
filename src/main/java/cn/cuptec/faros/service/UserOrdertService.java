package cn.cuptec.faros.service;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.common.utils.OrderNumberUtil;
import cn.cuptec.faros.common.utils.sms.HttpUtils;
import cn.cuptec.faros.config.com.Url;
import cn.cuptec.faros.config.datascope.DataScope;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.dto.BOrderReqData;
import cn.cuptec.faros.dto.KuaiDiXiaDanParam;
import cn.cuptec.faros.dto.XiaDanParam;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.mapper.UserOrderMapper;
import cn.cuptec.faros.vo.MapExpressTrackVo;
import cn.cuptec.faros.vo.SubscribeVO;
import cn.cuptec.faros.vo.UOrderStatuCountVo;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.kuaidi100.sdk.api.BOrderOfficial;
import com.kuaidi100.sdk.api.Subscribe;
import com.kuaidi100.sdk.contant.ApiInfoConstant;
import com.kuaidi100.sdk.contant.CompanyConstant;
import com.kuaidi100.sdk.core.IBaseClient;
import com.kuaidi100.sdk.pojo.HttpResult;
import com.kuaidi100.sdk.request.*;
import com.kuaidi100.sdk.utils.SignUtils;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


@Slf4j
@Service
public class UserOrdertService extends ServiceImpl<UserOrderMapper, UserOrder> {
    @Resource
    private UserService userService;
    @Resource
    private ProductService productService;
    @Resource
    private DeviceScanSignLogService deviceScanSignLogService;
    @Resource
    private ProductStockService productStockService;
    @Resource
    private PlanUserService planUserService;

    @Resource
    private UserRoleService userRoleService;
    @Resource
    private ServicePackService servicePackService;
    @Resource
    private cn.cuptec.faros.service.WxMpService wxMpService;
    @Resource
    private DeliverySettingService deliverySettingService;
    @Resource
    private DeliveryInfoService deliveryInfoService;
    @Autowired
    public RedisTemplate redisTemplate;
    private String borderApiUrl = "https://poll.kuaidi100.com/order/borderapi.do";


    /**
     * 商家寄件 用户下单付款自动叫订单
     *
     * @return
     */

    public void autoXiaDanCheck(String userOrderNo, String url) {
        //判断代理商是否开启了自动下单

        UserOrder userOrder = getOne(new QueryWrapper<UserOrder>().lambda().eq(UserOrder::getOrderNo, userOrderNo));

        DeliverySetting deliverySetting = deliverySettingService.getOne(new QueryWrapper<DeliverySetting>().lambda().eq(DeliverySetting::getDeptId, userOrder.getDeptId()));
        if (deliverySetting == null || deliverySetting.getStatus().equals(0)) {
            return;
        }
        //查看用户选择的发货时间
        LocalDate deliveryDate = userOrder.getDeliveryDate();//期望发货时间
        LocalDate now = LocalDate.now();
        long until = now.until(deliveryDate, ChronoUnit.DAYS);
        if (until == 0) {
            autoXiaDan(userOrderNo, url);
        }else {
            //加入定时任务到期自动下单
            String keyRedis = String.valueOf(StrUtil.format("{}{}", "autoXiaDan:", userOrder.getId()+"/"+url));
            redisTemplate.opsForValue().set(keyRedis,userOrderNo, until, TimeUnit.DAYS);//设置过期时间

        }


    }

    public void autoXiaDan(String userOrderNo, String url) {
        UserOrder userOrder = getOne(new QueryWrapper<UserOrder>().lambda().eq(UserOrder::getOrderNo, userOrderNo));
        DeliverySetting deliverySetting = deliverySettingService.getOne(new QueryWrapper<DeliverySetting>().lambda().eq(DeliverySetting::getDeptId, userOrder.getDeptId()));

        ServicePack servicePack = servicePackService.getById(userOrder.getServicePackId());

        PrintReq printReq = new PrintReq();
        BOrderReq bOrderReq = new BOrderReq();
        bOrderReq.setKuaidicom(CompanyConstant.SF);
        bOrderReq.setSendManName(deliverySetting.getName());
        bOrderReq.setSendManMobile(deliverySetting.getPhone());
        bOrderReq.setSendManPrintAddr(deliverySetting.getAddress());
        bOrderReq.setRecManName(userOrder.getReceiverName());
        bOrderReq.setRecManMobile(userOrder.getReceiverPhone());
        bOrderReq.setRecManPrintAddr(userOrder.getReceiverDetailAddress());
        bOrderReq.setCallBackUrl(url + "purchase/order/autoXiaDankuaidicallback");
        bOrderReq.setWeight(servicePack.getWeight() + "");
        bOrderReq.setReturnType("20");
        bOrderReq.setPickupEndTime("18:00");
        bOrderReq.setPickupStartTime("09:00");
        String t = String.valueOf(System.currentTimeMillis());
        String param = new Gson().toJson(bOrderReq);

        printReq.setKey("JAnUGrLl5945");
        printReq.setSign(SignUtils.printSign(param, t, "JAnUGrLl5945", "75bd152004314af288765416804ac830"));
        printReq.setT(t);
        printReq.setParam(param);
        printReq.setMethod(ApiInfoConstant.B_ORDER_OFFICIAL_ORDER_METHOD);
        System.out.println(printReq);
        IBaseClient bOrder = new BOrderOfficial();
        try {
            HttpResult execute = bOrder.execute(printReq);
            String body = execute.getBody();
            XiaDanParam xiaDanParam = new Gson().fromJson(body, XiaDanParam.class);
            userOrder.setTaskId(xiaDanParam.getData().getTaskId());
            userOrder.setMessage(xiaDanParam.getMessage());
            userOrder.setDeliverySn(xiaDanParam.getData().getKuaidinum());
            updateById(userOrder);
            DeliveryInfo deliveryInfo = new DeliveryInfo();
            deliveryInfo.setTaskId(xiaDanParam.getData().getTaskId());
            deliveryInfo.setMessage(xiaDanParam.getMessage());
            deliveryInfo.setUserOrderNo(userOrderNo);
            deliveryInfo.setDeliveryName("SF");
            deliveryInfo.setDeliverySn(xiaDanParam.getData().getKuaidinum());
            deliveryInfoService.save(deliveryInfo);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        LocalDate deliveryDate = LocalDate.now();//期望发货时间
        LocalDate now = LocalDate.now();
        long until = now.until(deliveryDate, ChronoUnit.DAYS);
        if (until == 0) {
            System.out.println(until);
        }
        System.out.println(until);
//        PrintReq printReq = new PrintReq();
//        BOrderReq bOrderReq = new BOrderReq();
//        bOrderReq.setKuaidicom(CompanyConstant.SF);
//        bOrderReq.setSendManName("张三");
//        bOrderReq.setSendManMobile("13307637744");
//        bOrderReq.setSendManPrintAddr("浙江省杭州市余杭区马鞍山雅苑");
//        bOrderReq.setRecManName("李四");
//        bOrderReq.setRecManMobile("13307637744");
//        bOrderReq.setRecManPrintAddr("浙江省杭州市余杭区西溪北苑121幢");
//        bOrderReq.setCallBackUrl("http://www.baidu.com");
//        bOrderReq.setWeight("1");
//        bOrderReq.setPickupEndTime("14:00");
//        bOrderReq.setPickupStartTime("15:00");
//        String t = String.valueOf(System.currentTimeMillis());
//        String param = new Gson().toJson(bOrderReq);
//
//        printReq.setKey("JAnUGrLl5945");
//        printReq.setSign(SignUtils.printSign(param, t, "JAnUGrLl5945", "75bd152004314af288765416804ac830"));
//        printReq.setT(t);
//        printReq.setParam(param);
//        printReq.setMethod(ApiInfoConstant.B_ORDER_OFFICIAL_ORDER_METHOD);
//        System.out.println(printReq);
//        IBaseClient bOrder = new BOrderOfficial();
//        try {
//            HttpResult execute = bOrder.execute(printReq);
//            String body = execute.getBody();
//            XiaDanParam xiaDanParam = new Gson().fromJson(body, XiaDanParam.class);
//            System.out.println(body);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }
//
//
//    public static void testBorderOfficial() {
//        PrintReq printReq = new PrintReq();
//        BOrderReq bOrderReq = new BOrderReq();
//        bOrderReq.setKuaidicom(CompanyConstant.SF);
//        bOrderReq.setSendManName("张三");
//        bOrderReq.setSendManMobile("13633407133");
//        bOrderReq.setSendManPrintAddr("浙江省杭州市余杭区");
//        bOrderReq.setRecManName("李四");
//        bOrderReq.setRecManMobile("13307637744");
//        bOrderReq.setRecManPrintAddr("浙江省杭州市西湖区");
//        // bOrderReq.setCallBackUrl(url.getUrl() + "purchase/order/kuaidicallback");
//        bOrderReq.setCallBackUrl("http://www.baidu.com");
//        bOrderReq.setWeight("1");
//
//        String t = String.valueOf(System.currentTimeMillis());
//        String param = new Gson().toJson(bOrderReq);
//        //使用的红小豆快递100
//        printReq.setKey("JAnUGrLl5945");
//        printReq.setSign(SignUtils.printSign(param, t, "JAnUGrLl5945", "75bd152004314af288765416804ac830"));
//        printReq.setT(t);
//        printReq.setParam(param);
//        printReq.setMethod(ApiInfoConstant.B_ORDER_OFFICIAL_ORDER_METHOD);
//        IBaseClient bOrder = new BOrderOfficial();
//        HttpResult execute = null;
//        try {
//            execute = bOrder.execute(printReq);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        System.out.println(execute);
//    }
//

//    public void reduceAmounts(UserOrder userOrder) {
//        List<Double> reduceAmountList = new ArrayList<>();
//        double a = 0.0;
//        reduceAmountList.add(a);
//        for (int i = 1; i < 100; i++) {
//            a = a + 0.01;
//            BigDecimal b = new BigDecimal(a);
//            double f1 = b.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue();
//            reduceAmountList.add(f1);
//        }
//        //查询本部门的业务员
//        User user = userService.getById(userOrder.getSalesmanId());
//        Integer deptId = user.getDeptId();
//        List<Integer> uIds = new ArrayList<>();
//        if (deptId != null) {
//            List<User> users = userService.list(new QueryWrapper<User>().lambda().eq(User::getDeptId, deptId));
//            if (!CollectionUtils.isEmpty(users)) {
//                uIds.addAll(users.stream().map(User::getId)
//                        .collect(Collectors.toList()));
//            }
//        }
//        LambdaQueryWrapper<UserOrder> queryWrapper = new LambdaQueryWrapper<>();
//        queryWrapper.eq(UserOrder::getStatus, 1);
//        queryWrapper.in(UserOrder::getSalesmanId, uIds);
//        List<UserOrder> userOrdersNoPay = list(queryWrapper);
//        if (CollectionUtils.isEmpty(userOrdersNoPay)) {
//            BigDecimal price = userOrder.getSalePrice().subtract(new BigDecimal(0.00));
//
//            userOrder.setPayment(price.setScale(2, BigDecimal.ROUND_HALF_UP));
//            userOrder.setReduceAmount(0.00);
//        } else {
//            //确定要减少的支付金额  取最小
//            List<Double> reduceAmounts = userOrdersNoPay.stream().map(UserOrder::getReduceAmount)
//                    .collect(Collectors.toList());
//            reduceAmountList.addAll(reduceAmounts);
//            //差集
//            reduceAmountList.removeAll(reduceAmounts);
//            //排序找最小值
//            Collections.sort(reduceAmountList);
//            Double aDouble = reduceAmountList.get(0);
//            userOrder.setPayment((userOrder.getSalePrice().subtract(new BigDecimal(aDouble))).setScale(2, BigDecimal.ROUND_HALF_UP));
//            userOrder.setReduceAmount(aDouble);
//        }
//
//    }

    public boolean saveUserOrder(UserOrder userOrder) {
        return super.save(userOrder);
    }
//    @Override
//    public boolean save(UserOrder userOrder) {
//        String orderNo = OrderNumberUtil.getLocalTrmSeqNum();//订单号
//        userOrder.setOrderNo(orderNo);
//        userOrder.setStatus(0);//下单状态
//        userOrder.setCreateTime(new Date());
//        userOrder.setAutomaticCreateTime(new Date());
//        userOrder.setUserId(SecurityUtils.getUser().getId());
//
//        //设置价格
//        Object productDetail = productService.getProductDetail(userOrder.getProductId(), userOrder.getSalesmanId());
//        if (productDetail instanceof Product) {
//            userOrder.setSalePrice(((Product) productDetail).getSalePrice());
//        } else if (productDetail instanceof CustomProduct) {
//            userOrder.setSalePrice(((CustomProduct) productDetail).getSalePrice());
//        }
//        if (userOrder.getSalesmanId() != null) {
//            User salesman = userService.getById(userOrder.getSalesmanId());
//            if (salesman == null) throw new RuntimeException("请选择正确的业务员");
//            if (salesman.getConfirmOrder() == 1) {
//                userOrder.setStatus(1);
//                userOrder.setConfirmTime(new Date());
//                //如果设置自动跳过确认订单步骤 则 自动减价格
//                List<SalesmanPayChannel> list = salesmanPayChannelService.list(new QueryWrapper<SalesmanPayChannel>().lambda().eq(SalesmanPayChannel::getSalesmanId, userOrder.getSalesmanId()));
//                if (CollectionUtils.isEmpty(list) || list.get(0).getPayType() == 1) {
//                    reduceAmounts(userOrder);
//                }
//            }
//            userOrder.setDeptId(salesman.getDeptId() == null ? 1 : salesman.getDeptId());
//        } else {
//            //否则设置为总部
//            userOrder.setDeptId(1);
//        }
//        //添加用户绑定医生
////        UserDoctorRelation userDoctorRelation = new UserDoctorRelation();
////        userDoctorRelation.setDoctorId(userOrder.getDoctorId());
////        userDoctorRelation.setUserId(userOrder.getUserId());
////        userDoctorRelationService.save(userDoctorRelation);
//        return super.save(userOrder);
//    }

    public IPage<UserOrder> listMyOrder(IPage<UserOrder> page, Wrapper<UserOrder> queryWrapper) {
        return baseMapper.pageMyOrder(page, queryWrapper);
    }

    @Override
    public UserOrder getById(Serializable id) {
        UserOrder userOrder = baseMapper.selectById(id);
        return userOrder;
    }


    public UOrderStatuCountVo countScoped() {
        Boolean aBoolean = userRoleService.judgeUserIsAdmin(SecurityUtils.getUser().getId());
        LocalDate now = LocalDate.now().plusDays(1);

        DataScope dataScope = new DataScope();
        if (aBoolean) {
            dataScope.setIsOnly(false);
        } else {
            dataScope.setIsOnly(true);
        }

        List<UserOrder> userOrders = baseMapper.listScoped(Wrappers.<UserOrder>lambdaQuery().eq(UserOrder::getTest, 0), dataScope);
        // count0 = userOrders.stream().filter(it -> it.getStatus() == 0).count();
        long count1 = userOrders.stream().filter(it -> it.getStatus() == 1).count();
        long count2 = userOrders.stream().filter(it -> it.getStatus() == 2).count();
        long count3 = userOrders.stream().filter(it -> it.getStatus() == 3).count();
        long count4 = userOrders.stream().filter(it -> it.getStatus() == 4).count();
        long count6 = userOrders.stream().filter(it -> it.getStatus() == 5).count();
        long count5 = userOrders.stream().filter(it -> it.getDeliveryDate().isBefore(now) && it.getStatus() == 2).count();
        UOrderStatuCountVo vo = new UOrderStatuCountVo();
        vo.setStatu0(userOrders.stream().count());//全部
        vo.setStatu1(count1);//待付款
        vo.setStatu2(count2);//待发货
        vo.setStatu3(count3);//待收货
        vo.setStatu4(count4);//已收货
        vo.setStatu5(count5);//期待今日发货
        vo.setStatu6(count6);//已回收
        return vo;
    }

    //查询部门订单
    public IPage<UserOrder> pageScoped(Boolean admin, IPage<UserOrder> page, Wrapper<UserOrder> queryWrapper) {
        DataScope dataScop3 = new DataScope();
        dataScop3.setIsOnly(!admin);
        return baseMapper.pageScoped(page, queryWrapper, dataScop3);
    }

    //查询部门订单
    public List<UserOrder> scoped(Wrapper<UserOrder> queryWrapper) {

        return baseMapper.scoped(queryWrapper);
    }

    //
//    //手动修改订单
//    public void updateOrderManual(UserOrder order) {
//        Assert.notNull(order.getStatus(), "订单状态不能为空");
//        switch (order.getStatus()) {
//            case 0: {
//                order.setConfirmTime(null);
//                order.setDelieveyTime(null);
//                order.setCompleteTime(null);
//                break;
//            }
//            case 1: {
//                order.setConfirmTime(new Date());
//                break;
//            }
//            case 2: {
//                Assert.notNull(order.getProductSn(), "对应的产品序列号不能为空");
//                break;
//            }
//            case 3: {
//                order.setCompleteTime(new Date());
//                break;
//            }
//        }
//        updateById(order);
//    }
//
    @Transactional(rollbackFor = Exception.class)
    public void conformDelivery(int orderId, String deliveryCompanyCode, String deliveryNumber,
                                String productSn1, String productSn2, String productSn3) {
        UserOrder userOrder = super.getById(orderId);

        if (!StringUtils.isEmpty(productSn1)) {
            Integer userId = userOrder.getUserId();
            TbTrainUser infoByUXtUserId = planUserService.getInfoByUXtUserId(userId);
            if (infoByUXtUserId != null) {
                String userId1 = infoByUXtUserId.getUserId();
                List<ProductStock> list = productStockService.list(new QueryWrapper<ProductStock>().lambda()
                        .eq(ProductStock::getProductSn, productSn1)
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


        Assert.notNull(userOrder, "订单不存在");

        Assert.isTrue(userOrder.getStatus() == 2, "订单非待发货状态");
        userOrder.setProductSn1(productSn1);
        userOrder.setProductSn2(productSn2);
        userOrder.setProductSn3(productSn3);
        userOrder.setStatus(3);
        userOrder.setDeliveryCompanyCode(deliveryCompanyCode);
        userOrder.setDeliverySn(deliveryNumber);
        userOrder.setDeliveryNumber(deliveryNumber);
        userOrder.setDeliveryTime(new Date());
        if (userOrder.getOrderType() != null && userOrder.getOrderType().equals(1)) {

            userOrder.setMoveTime(LocalDateTime.now());
        }
        super.updateById(userOrder);
        SubscribeVO subscribeVO = new SubscribeVO();
        subscribeVO.setLogisticsNo(deliveryNumber);
        subscribeVO.setLogisticsCode(deliveryCompanyCode);
        subscribeVO.setPhone(userOrder.getReceiverPhone());
        try {
            subscribe(subscribeVO);
        } catch (Exception e) {
            e.printStackTrace();
        }
        User byId = userService.getById(userOrder.getUserId());
        String deliveryCompany = "";
        switch (deliveryCompanyCode) {
            case "jd":
                deliveryCompany = "京东";
                break; //可选
            case "debangkuaidi":
                deliveryCompany = "德邦";
                break; //可选
            case "shunfeng":
                deliveryCompany = "顺丰";
                break; //可选
            case "jtexpress":
                deliveryCompany = "极兔";
                break; //可选
            case "yuantong":
                deliveryCompany = "圆通";
                break; //可选
            case "shentong":
                deliveryCompany = "申通";
                break; //可选
            case "zhongtong":
                deliveryCompany = "中通";
                break; //可选
            case "yunda":
                deliveryCompany = "韵达";
                break; //可选
            case "youzhengguonei":
                deliveryCompany = "邮政";
                break; //可选
            case "huitongkuaidi":
                deliveryCompany = "百世";
                break; //可选
        }
        //发送公众号通知
        DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        if (byId != null) {
            wxMpService.shipNotice(byId.getMpOpenId(), "您的订单已发货", userOrder.getOrderNo(), deliveryCompany, deliveryNumber,
                    df.format(LocalDateTime.now()), "点击查看详情", "pages/myOrder/myOrder");
        }


        //判断是租赁和订单，从设备发货，确定“开始使用时间” 加入倒计时推送
        if (userOrder.getOrderType().equals(1)) {
            for (int i = 1; i < 4; i++) {
                String keyRedis = String.valueOf(StrUtil.format("{}{}", "serviceNotice" + i + "3:", userOrder.getId()));
                LocalDateTime localDateTime = LocalDateTime.now().plusMonths(i);
                LocalDateTime localDateTime3 = localDateTime.minusDays(3);
                Duration sjc = Duration.between(LocalDateTime.now(), localDateTime3);// 计算时间差
                redisTemplate.opsForValue().set(keyRedis, userOrder.getId(), sjc.toDays(), TimeUnit.DAYS);//设置过期时间
                //5天
                keyRedis = String.valueOf(StrUtil.format("{}{}", "serviceNotice" + i + "5:", userOrder.getId()));
                LocalDateTime localDateTime5 = localDateTime.minusDays(5);
                Duration sjc5 = Duration.between(LocalDateTime.now(), localDateTime5);// 计算时间差
                redisTemplate.opsForValue().set(keyRedis, userOrder.getId(), sjc5.toDays(), TimeUnit.DAYS);//设置过期时间
                //7天
                keyRedis = String.valueOf(StrUtil.format("{}{}", "serviceNotice" + i + "7:", userOrder.getId()));
                LocalDateTime localDateTime7 = localDateTime.minusDays(7);
                Duration sjc7 = Duration.between(LocalDateTime.now(), localDateTime7);// 计算时间差
                redisTemplate.opsForValue().set(keyRedis, userOrder.getId(), sjc7.toDays(), TimeUnit.DAYS);//设置过期时间

            }

        }
    }

    public void subscribe(SubscribeVO subscribeVO) throws Exception {

        SubscribeParameters subscribeParameters = new SubscribeParameters();
        subscribeParameters.setCallbackurl("https://pharos3.ewj100.com/purchase/order/subscribe_Callback");
        subscribeParameters.setPhone(subscribeVO.getPhone());
        subscribeParameters.setResultv2("6");

        SubscribeParam subscribeParam = new SubscribeParam();
        subscribeParam.setParameters(subscribeParameters);
        subscribeParam.setCompany(subscribeVO.getLogisticsCode());
        subscribeParam.setNumber(subscribeVO.getLogisticsNo());
        subscribeParam.setKey("JAnUGrLl5945");

        SubscribeReq subscribeReq = new SubscribeReq();
        subscribeReq.setSchema(ApiInfoConstant.SUBSCRIBE_SCHEMA);
        subscribeReq.setParam(new Gson().toJson(subscribeParam));

        IBaseClient subscribe = new Subscribe();


        HttpResult result = subscribe.execute(subscribeReq);
        String body = result.getBody();
        JSONObject jsonObject = JSONObject.parseObject(body);

        String message = jsonObject.getString("message");
        log.info("message:" + message);

    }
}
