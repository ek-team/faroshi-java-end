package cn.cuptec.faros.service;

import cn.cuptec.faros.common.utils.OrderNumberUtil;
import cn.cuptec.faros.common.utils.StringUtils;
import cn.cuptec.faros.common.utils.sms.HttpUtils;
import cn.cuptec.faros.config.datascope.DataScope;
import cn.cuptec.faros.config.security.util.SecurityUtils;
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
import com.kuaidi100.sdk.api.Subscribe;
import com.kuaidi100.sdk.contant.ApiInfoConstant;
import com.kuaidi100.sdk.core.IBaseClient;
import com.kuaidi100.sdk.pojo.HttpResult;
import com.kuaidi100.sdk.request.SubscribeParam;
import com.kuaidi100.sdk.request.SubscribeParameters;
import com.kuaidi100.sdk.request.SubscribeReq;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserOrdertService extends ServiceImpl<UserOrderMapper, UserOrder> {

    @Resource
    private UserService userService;
    @Resource
    private ProductService productService;
    @Resource
    private ExpressService expressService;
    @Resource
    private ProductStockService productStockService;
    @Resource
    private LocatorService locatorService;

    @Resource
    private UserRoleService userRoleService;
    @Resource
    private SalesmanPayChannelService salesmanPayChannelService;
    @Resource
    private cn.cuptec.faros.service.WxMpService wxMpService;

    public String queryStorehouseByProductSn(String productSn, QueryWrapper queryWrapper) {


        return null;
    }

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
        DataScope dataScope = new DataScope();
        dataScope.setIsOnly(true);
        List<UserOrder> userOrders = baseMapper.listScoped(Wrappers.<UserOrder>lambdaQuery(), dataScope);
        long count0 = userOrders.stream().filter(it -> it.getStatus() == 0).count();
        long count1 = userOrders.stream().filter(it -> it.getStatus() == 1).count();
        long count2 = userOrders.stream().filter(it -> it.getStatus() == 2).count();
        long count3 = userOrders.stream().filter(it -> it.getStatus() == 3).count();
        long count4 = userOrders.stream().filter(it -> it.getStatus() == 4).count();
        UOrderStatuCountVo vo = new UOrderStatuCountVo();
        vo.setStatu0(count0);
        vo.setStatu1(count1);
        vo.setStatu2(count2);
        vo.setStatu3(count3);
        vo.setStatu4(count4);
        return vo;
    }

    //查询部门订单
    public IPage<UserOrder> pageScoped(IPage<UserOrder> page, Wrapper<UserOrder> queryWrapper) {
        DataScope dataScop3 = new DataScope();
        dataScop3.setIsOnly(true);
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
    public void conformDelivery(int orderId, String deliveryCompanyCode, String deliveryNumber) {
        UserOrder userOrder = super.getById(orderId);
        Assert.notNull(userOrder, "订单不存在");

        Assert.isTrue(userOrder.getStatus() == 2, "订单非待发货状态");

        userOrder.setStatus(3);
        userOrder.setDeliveryCompanyCode(deliveryCompanyCode);
        userOrder.setDeliverySn(deliveryNumber);
        userOrder.setDeliveryNumber(deliveryNumber);
        userOrder.setDeliveryTime(new Date());
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
        wxMpService.shipNotice(byId.getMpOpenId(), "您的订单已发货", userOrder.getOrderNo(), deliveryCompany, deliveryNumber,
                df.format(LocalDateTime.now()), "点击查看详情", "pages/myOrder/myOrder");

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
