package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.com.Url;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.service.*;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.api.R;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 退款审核
 */
@AllArgsConstructor
@Slf4j
@RestController
@RequestMapping("/reviewRefundOrder")
public class ReviewRefundOrderController extends AbstractBaseController<ReviewRefundOrderService, ReviewRefundOrder> {

    @Resource
    private UserService userService;
    @Resource
    private RetrieveOrderService retrieveOrderService;
    @Resource
    private UserOrdertService userOrdertService;
    @Resource
    private OrderRefundInfoService orderRefundInfoService;
    @Resource
    private DeptService deptService;
    @Resource
    private AliPayService aliPayService;
    @Resource
    private UpdateOrderRecordService updateOrderRecordService;
    private final Url urlData;

    @Override
    protected Class<ReviewRefundOrder> getEntityClass() {
        return ReviewRefundOrder.class;
    }


    /**
     * 列表查询
     *
     * @return
     */
    @GetMapping("/page")
    public RestResponse page(@RequestParam(value = "userOrderNo", required = false) String userOrderNo) {
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());

        if (!StringUtils.isEmpty(userOrderNo)) {
            RetrieveOrder retrieveOrder = retrieveOrderService.getOne(new QueryWrapper<RetrieveOrder>().lambda()
                    .like(RetrieveOrder::getUserOrderNo, userOrderNo)
            );
            if (retrieveOrder == null) {
                return RestResponse.ok(new Page<>());
            }
            queryWrapper.eq("retrieve_order_no", retrieveOrder.getOrderNo());
        }
        Page<ReviewRefundOrder> page = getPage();
        queryWrapper.orderByDesc("create_time");
        queryWrapper.orderByDesc("status");
        IPage page1 = service.page(page, queryWrapper);
        List<ReviewRefundOrder> records = page1.getRecords();
        if (!CollectionUtils.isEmpty(records)) {
            List<String> retrieveOrders = records.stream().map(ReviewRefundOrder::getRetrieveOrderNo)
                    .collect(Collectors.toList());
            List<RetrieveOrder> retrieveOrderList = retrieveOrderService.list(new QueryWrapper<RetrieveOrder>().lambda()
                    .in(RetrieveOrder::getOrderNo, retrieveOrders));
            Map<String, RetrieveOrder> retrieveOrderMap = retrieveOrderList.stream()
                    .collect(Collectors.toMap(RetrieveOrder::getOrderNo, t -> t));
            List<OrderRefundInfo> orderRefundInfoList = orderRefundInfoService.list(new QueryWrapper<OrderRefundInfo>().lambda()
                    .in(OrderRefundInfo::getOrderId, retrieveOrders)
                    .eq(OrderRefundInfo::getRefundStatus, 2));
            Map<String, List<OrderRefundInfo>> servicePackProductPicMap = orderRefundInfoList.stream()
                    .collect(Collectors.groupingBy(OrderRefundInfo::getOrderId));
            for (ReviewRefundOrder reviewRefundOrder : records) {
                RetrieveOrder retrieveOrder = retrieveOrderMap.get(reviewRefundOrder.getRetrieveOrderNo());
                if (retrieveOrder != null) {
                    reviewRefundOrder.setUserOrderNo(retrieveOrder.getUserOrderNo());
                }
                List<OrderRefundInfo> orderRefundInfoList1 = servicePackProductPicMap.get(reviewRefundOrder.getRetrieveOrderNo());
                BigDecimal totalRefundFee = new BigDecimal("0");
                if (!CollectionUtils.isEmpty(orderRefundInfoList1)) {
                    for (OrderRefundInfo refundInfo : orderRefundInfoList1) {
                        totalRefundFee = totalRefundFee.add(refundInfo.getRefundFee().divide(new BigDecimal("100")));

                    }
                    reviewRefundOrder.setTotalRefundFee(totalRefundFee);
                }

            }
        }
        page1.setRecords(records);
        return RestResponse.ok(page1);
    }


    /**
     * 添加
     *
     * @return
     */
    @PostMapping("/add")
    public RestResponse add(@RequestBody ReviewRefundOrder reviewRefundOrder) {
        RetrieveOrder retrieveOrder = retrieveOrderService.getOne(new QueryWrapper<RetrieveOrder>().lambda()
                .eq(RetrieveOrder::getOrderNo, reviewRefundOrder.getRetrieveOrderNo()));
        UserOrder userOrderOne = userOrdertService.getById(retrieveOrder.getOrderId());
        List<OrderRefundInfo> orderRefundInfoList = orderRefundInfoService.list(new QueryWrapper<OrderRefundInfo>().lambda()
                .in(OrderRefundInfo::getOrderId, reviewRefundOrder.getRetrieveOrderNo())
                .eq(OrderRefundInfo::getRefundStatus, 2));
        BigDecimal totalAmount = reviewRefundOrder.getRefundFee();
        if (!CollectionUtils.isEmpty(orderRefundInfoList)) {
            BigDecimal totalRefundFee = new BigDecimal("0");
            for (OrderRefundInfo refundInfo : orderRefundInfoList) {
                totalRefundFee = totalRefundFee.add(refundInfo.getRefundFee().divide(new BigDecimal("100")));

            }
            totalAmount = reviewRefundOrder.getRefundFee().add(totalRefundFee);

        }
        if (totalAmount.doubleValue() > userOrderOne.getPayment().doubleValue()) {
            return RestResponse.failed("金额不能大于实际付款金额");
        }


        reviewRefundOrder.setCreateTime(LocalDateTime.now());
        reviewRefundOrder.setStatus(3);
        User byId = userService.getById(SecurityUtils.getUser().getId());

        reviewRefundOrder.setCreateName(byId.getNickname());


        UserOrder userOrder = new UserOrder();
        userOrder.setId(Integer.parseInt(retrieveOrder.getOrderId()));
        userOrder.setRefundInitiationTime(LocalDateTime.now());


        UpdateOrderRecord updateOrderRecord = new UpdateOrderRecord();
        updateOrderRecord.setOrderId(Integer.parseInt(retrieveOrder.getOrderId()));
        updateOrderRecord.setCreateUserId(SecurityUtils.getUser().getId());
        updateOrderRecord.setCreateTime(LocalDateTime.now());
        updateOrderRecord.setDescStr("退款审核");
        updateOrderRecordService.save(updateOrderRecord);
        service.save(reviewRefundOrder);
        userOrder.setReviewRefundOrderId(reviewRefundOrder.getId());
        userOrdertService.updateById(userOrder);
        if (reviewRefundOrder.getType().equals(0)) {
            retrieveOrderService.update(Wrappers.<RetrieveOrder>lambdaUpdate()
                    .eq(RetrieveOrder::getOrderNo, reviewRefundOrder.getRetrieveOrderNo())
                    .set(RetrieveOrder::getStatus, 6)
                    .set(RetrieveOrder::getReviewRefundOrderId, reviewRefundOrder.getId()));
        } else {
            retrieveOrderService.update(Wrappers.<RetrieveOrder>lambdaUpdate()
                    .eq(RetrieveOrder::getOrderNo, reviewRefundOrder.getRetrieveOrderNo())
                    .set(RetrieveOrder::getStatus, 8)
                    .set(RetrieveOrder::getReviewRefundOrderId, reviewRefundOrder.getId()));
        }

        return RestResponse.ok();
    }

    /**
     * 审核
     *
     * @param id
     * @param reviewStatus
     * @return
     */
    @GetMapping("/review")
    public RestResponse review(@RequestParam("reviewRefundDesc") String reviewRefundDesc, @RequestParam("id") Integer id, @RequestParam("reviewStatus") Integer reviewStatus) {
        ReviewRefundOrder reviewRefundOrder = service.getById(id);
        Double amount = reviewRefundOrder.getRefundFee().doubleValue();
        RetrieveOrder retrieveOrder = retrieveOrderService.getOne(new QueryWrapper<RetrieveOrder>().lambda()
                .eq(RetrieveOrder::getOrderNo, reviewRefundOrder.getRetrieveOrderNo()));
        UserOrder updateUserOrder = new UserOrder();
        reviewRefundOrder.setReviewRefundDesc(reviewRefundDesc);
        if (reviewStatus.equals(1)) {
            //退款

            Integer status = retrieveOrder.getStatus();
            if (reviewRefundOrder.getType().equals(0)) {
                if (!status.equals(6)) {
                    return RestResponse.ok();
                }
            }


            UserOrder userOrder = userOrdertService.getById(retrieveOrder.getOrderId());
            List<OrderRefundInfo> orderRefundInfoList = orderRefundInfoService.list(new QueryWrapper<OrderRefundInfo>().lambda()
                    .in(OrderRefundInfo::getOrderId, reviewRefundOrder.getRetrieveOrderNo())
                    .eq(OrderRefundInfo::getRefundStatus, 2));
            BigDecimal totalAmount = reviewRefundOrder.getRefundFee();
            if (!CollectionUtils.isEmpty(orderRefundInfoList)) {
                BigDecimal totalRefundFee = new BigDecimal("0");
                for (OrderRefundInfo refundInfo : orderRefundInfoList) {
                    totalRefundFee = totalRefundFee.add(refundInfo.getRefundFee().divide(new BigDecimal("100")));

                }
                totalAmount = reviewRefundOrder.getRefundFee().add(totalRefundFee);

            }
            if (totalAmount.doubleValue() > userOrder.getPayment().doubleValue()) {
                return RestResponse.failed("金额不能大于实际付款金额");
            }

            //添加退款记录
            OrderRefundInfo orderRefunds = new OrderRefundInfo();
            orderRefunds.setReviewRefundOrderId(reviewRefundOrder.getId());
            orderRefunds.setOrderId(retrieveOrder.getOrderNo());
            orderRefunds.setRefundReason(reviewRefundOrder.getRefundReason());
            orderRefunds.setRefundFee(new BigDecimal(amount).multiply(new BigDecimal("100")));
            orderRefunds.setCreateId(SecurityUtils.getUser().getId());
            orderRefunds.setCreateTime(new Date());
            orderRefunds.setRefundStatus(1);
            orderRefunds.setRetrieveOrderId(retrieveOrder.getId());
            orderRefunds.setOrderRefundNo(IdUtil.getSnowflake(0, 0).nextIdStr());
            orderRefunds.setTransactionId(userOrder.getTransactionId());
            orderRefundInfoService.saveOrUpdate(orderRefunds);


            Dept dept = deptService.getById(userOrder.getDeptId());
            if (userOrder.getPayType() == null || userOrder.getPayType().equals(1)) {
                //微信退款
                String url = urlData.getRefundUrl() + "?orderNo=" + orderRefunds.getOrderRefundNo() + "&transactionId=" + userOrder.getTransactionId() + "&subMchId=" + dept.getSubMchId() + "&totalFee=" + userOrder.getPayment().multiply(new BigDecimal(100)).intValue() + "&refundFee=" + new BigDecimal(amount + "").multiply(new BigDecimal(100)).intValue();

                //String url = "https://api.redadzukibeans.com/weChat/wxpay/otherRefundOrder?orderNo=" + retrieveOrder.getOrderId() + "&transactionId=" + userOrder.getTransactionId() + "&subMchId=" + dept.getSubMchId() + "&totalFee=" + userOrder.getPayment().multiply(new BigDecimal(100)).intValue() + "&refundFee=" + new BigDecimal(amount).multiply(new BigDecimal(100)).intValue();
                String result = HttpUtil.get(url);
                RestResponse restResponse = JSONObject.parseObject(result, RestResponse.class);
                if (restResponse.getCode() == 500) {
                    //退款失败
                    reviewRefundOrder.setStatus(4);
                    reviewRefundOrder.setFailureReason(restResponse.getMsg());
                    service.updateById(reviewRefundOrder);
                    if (reviewRefundOrder.getType().equals(0)) {
                        retrieveOrderService.update(Wrappers.<RetrieveOrder>lambdaUpdate()
                                .eq(RetrieveOrder::getOrderNo, reviewRefundOrder.getRetrieveOrderNo())
                                .set(RetrieveOrder::getStatus, 3));
                    } else {
                        retrieveOrderService.update(Wrappers.<RetrieveOrder>lambdaUpdate()
                                .eq(RetrieveOrder::getOrderNo, reviewRefundOrder.getRetrieveOrderNo())
                                .set(RetrieveOrder::getStatus, 5));
                    }
                    return RestResponse.failed(restResponse.getMsg());

                }
                System.out.println(result);
            } else {
                //支付宝退款
                String s = aliPayService.aliRefundOrder(userOrder.getTransactionId(), new BigDecimal(amount + ""), orderRefunds.getOrderRefundNo());
                if (!StringUtils.isEmpty(s)) {
                    //退款失败
                    reviewRefundOrder.setStatus(4);
                    reviewRefundOrder.setFailureReason(s);
                    service.updateById(reviewRefundOrder);
                    if (reviewRefundOrder.getType().equals(0)) {
                        retrieveOrderService.update(Wrappers.<RetrieveOrder>lambdaUpdate()
                                .eq(RetrieveOrder::getOrderNo, reviewRefundOrder.getRetrieveOrderNo())
                                .set(RetrieveOrder::getStatus, 3));
                    } else {
                        retrieveOrderService.update(Wrappers.<RetrieveOrder>lambdaUpdate()
                                .eq(RetrieveOrder::getOrderNo, reviewRefundOrder.getRetrieveOrderNo())
                                .set(RetrieveOrder::getStatus, 5));
                    }
                    return RestResponse.failed(s);
                }

            }
            retrieveOrder.setStatus(4);


            retrieveOrderService.updateById(retrieveOrder);

            UpdateOrderRecord updateOrderRecord = new UpdateOrderRecord();
            updateOrderRecord.setOrderId(Integer.parseInt(retrieveOrder.getOrderId()));
            updateOrderRecord.setCreateUserId(SecurityUtils.getUser().getId());
            updateOrderRecord.setCreateTime(LocalDateTime.now());
            updateOrderRecord.setDescStr("退款成功");
            updateOrderRecordService.save(updateOrderRecord);
        }
        if (reviewStatus.equals(2)) {
            reviewRefundOrder.setStatus(2);
            //拒绝
            if (reviewRefundOrder.getType().equals(0)) {
                retrieveOrderService.update(Wrappers.<RetrieveOrder>lambdaUpdate()
                        .eq(RetrieveOrder::getOrderNo, reviewRefundOrder.getRetrieveOrderNo())
                        .set(RetrieveOrder::getStatus, 3));
            } else {
                retrieveOrderService.update(Wrappers.<RetrieveOrder>lambdaUpdate()
                        .eq(RetrieveOrder::getOrderNo, reviewRefundOrder.getRetrieveOrderNo())
                        .set(RetrieveOrder::getStatus, 5));
            }
        }


        service.updateById(reviewRefundOrder);


        updateUserOrder.setId(Integer.parseInt(retrieveOrder.getOrderId()));
        updateUserOrder.setRefundReviewTime(LocalDateTime.now());
        userOrdertService.updateById(updateUserOrder);
        return RestResponse.ok();
    }

    public static void main(String[] args) {
        String url = "https://api.redadzukibeans.com/weChat/wxpayother/otherRefundOrder?orderNo=1684857921172668416" + "&transactionId=4200001746202304208466576411" + "&subMchId=1634891163" + "&totalFee=" + new BigDecimal("9500").multiply(new BigDecimal(100)).intValue() + "&refundFee=" + new BigDecimal("9500").multiply(new BigDecimal(100)).intValue();
        String result = HttpUtil.get(url);

    }
}
