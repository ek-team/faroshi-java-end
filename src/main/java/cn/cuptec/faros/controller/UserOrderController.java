package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.common.utils.MapperUtil;
import cn.cuptec.faros.common.utils.OrderNumberUtil;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.service.*;
import cn.cuptec.faros.util.ExcelUtil;
import cn.cuptec.faros.vo.MapExpressTrackVo;
import cn.cuptec.faros.vo.UOrderStatuCountVo;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import me.chanjar.weixin.mp.bean.template.WxMpTemplateData;
import org.apache.commons.lang3.StringUtils;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/purchase/order")
public class UserOrderController extends AbstractBaseController<UserOrdertService, UserOrder> {

    @Resource
    private ServiceTypeService serviceTypeService;
    @Resource
    private UserServicePackageInfoService userServicePackageInfoService;
    @Resource
    private ServicePackService servicePackService;
    @Resource
    private ServicePackageInfoService servicePackageInfoService;

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
    public RestResponse pageScoped() {
        Page<UserOrder> page = getPage();
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());
        IPage<UserOrder> pageScoped = service.pageScoped(page, queryWrapper);
        if (CollUtil.isNotEmpty(pageScoped.getRecords())) {
            List<UserOrder> records = pageScoped.getRecords();

        }


        return RestResponse.ok(pageScoped);
    }

    //手动修改订单
    @PutMapping("/manage/updateOrder")
    public RestResponse updateOrderManual(@RequestBody UserOrder order) {
        service.updateById(order);
        return RestResponse.ok();
    }


    //发货
    @GetMapping("/manage/confirmDelivery")
    @Transactional
    public RestResponse confirDelivery(@RequestParam("id") int orderid, @RequestParam(value = "productSn") String productSn
            , @RequestParam(value = "expressCode", required = false) String expressCode, @RequestParam(value = "deliverySn", required = false) String deliverySn) {
        service.conformDelivery(orderid, productSn, expressCode, deliverySn);

        return RestResponse.ok();
    }


    /**
     * 购买者申请单的增加
     *
     * @param userOrder
     * @return
     */
    @PostMapping("/user/add")
    public RestResponse addOrder(@RequestBody UserOrder userOrder) {
        userOrder.setOrderNo(IdUtil.getSnowflake(0, 0).nextIdStr());
        userOrder.setStatus(0);
        userOrder.setCreateTime(LocalDateTime.now());
        userOrder.setUserId(SecurityUtils.getUser().getId());
        service.save(userOrder);
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

        return RestResponse.ok("产品下单成功!");
    }

    /**
     * 用户查询自己的订单
     */
    @GetMapping("/user/listMy")
    public RestResponse passApplyOrder() {
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());
        Page<UserOrder> page = getPage();
        queryWrapper.eq("user_id", SecurityUtils.getUser().getId());
        IPage iPage = service.listMyOrder(page, queryWrapper);

        return RestResponse.ok(iPage);
    }

    /**
     * 用户查询自己的订单详细信息
     */
    @GetMapping("/user/orderDetail")
    public RestResponse getMyOrderDetail(@RequestParam int id) {
        UserOrder userOrder = service.getById(id);

        return RestResponse.ok(userOrder);
    }

    @GetMapping("/getById")
    public RestResponse getById(@RequestParam String id) {
        UserOrder userOrder = service.getById(id);

        return RestResponse.ok(userOrder);
    }


    @Override
    protected Class<UserOrder> getEntityClass() {
        return UserOrder.class;
    }

}
