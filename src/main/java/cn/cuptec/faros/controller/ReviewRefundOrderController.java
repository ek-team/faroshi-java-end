package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.com.Url;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.service.*;
import cn.hutool.core.util.IdUtil;
import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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
    public RestResponse page() {
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());
        Page<ReviewRefundOrder> page = getPage();
        queryWrapper.orderByDesc("create_time");
        queryWrapper.orderByDesc("status");

        return RestResponse.ok(service.page(page, queryWrapper));
    }


    /**
     * 添加
     *
     * @return
     */
    @PostMapping("/add")
    public RestResponse add(@RequestBody ReviewRefundOrder reviewRefundOrder) {
        reviewRefundOrder.setCreateTime(LocalDateTime.now());
        reviewRefundOrder.setStatus(3);
        User byId = userService.getById(SecurityUtils.getUser().getId());

        reviewRefundOrder.setCreateName(byId.getNickname());
        RetrieveOrder one = retrieveOrderService.getOne(new QueryWrapper<RetrieveOrder>().lambda()
                .eq(RetrieveOrder::getOrderNo, reviewRefundOrder.getRetrieveOrderNo()));

        UserOrder userOrder = new UserOrder();
        userOrder.setId(Integer.parseInt(one.getOrderId()));
        userOrder.setRefundInitiationTime(LocalDateTime.now());


        UpdateOrderRecord updateOrderRecord = new UpdateOrderRecord();
        updateOrderRecord.setOrderId(Integer.parseInt(one.getOrderId()));
        updateOrderRecord.setCreateUserId(SecurityUtils.getUser().getId());
        updateOrderRecord.setCreateTime(LocalDateTime.now());
        updateOrderRecord.setDescStr("退款审核");
        updateOrderRecordService.save(updateOrderRecord);
        service.save(reviewRefundOrder);
        userOrder.setReviewRefundOrderId(reviewRefundOrder.getId());
        userOrdertService.updateById(userOrder);
        retrieveOrderService.update(Wrappers.<RetrieveOrder>lambdaUpdate()
                .eq(RetrieveOrder::getOrderNo, reviewRefundOrder.getRetrieveOrderNo())
                .set(RetrieveOrder::getStatus, 6)
                .set(RetrieveOrder::getReviewRefundOrderId, reviewRefundOrder.getId()));
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

        if (reviewStatus.equals(1)) {
            //退款

            Integer status = retrieveOrder.getStatus();
            if (!status.equals(6)) {
                return RestResponse.ok();
            }

            UserOrder userOrder = userOrdertService.getById(retrieveOrder.getOrderId());
            if (amount > userOrder.getPayment().doubleValue()) {
                return RestResponse.failed("金额不能大于实际付款金额");
            }

            //添加退款记录
            OrderRefundInfo orderRefunds = new OrderRefundInfo();
            orderRefunds.setOrderId(retrieveOrder.getOrderNo());
            orderRefunds.setRefundReason(reviewRefundOrder.getRefundReason());
            orderRefunds.setRefundFee(new BigDecimal(amount).multiply(new BigDecimal(100)));
            orderRefunds.setCreateId(SecurityUtils.getUser().getId());
            orderRefunds.setCreateTime(new Date());
            orderRefunds.setRefundStatus(1);
            orderRefunds.setRetrieveOrderId(retrieveOrder.getId());
            orderRefunds.setOrderRefundNo(IdUtil.getSnowflake(0, 0).nextIdStr());
            orderRefunds.setTransactionId(userOrder.getTransactionId());
            orderRefundInfoService.save(orderRefunds);


            Dept dept = deptService.getById(userOrder.getDeptId());
            if (userOrder.getPayType() == null || userOrder.getPayType().equals(1)) {
                //微信退款
                String url = urlData.getRefundUrl() + "?orderNo=" + retrieveOrder.getOrderNo() + "&transactionId=" + userOrder.getTransactionId() + "&subMchId=" + dept.getSubMchId() + "&totalFee=" + userOrder.getPayment().multiply(new BigDecimal(100)).intValue() + "&refundFee=" + new BigDecimal(amount + "").multiply(new BigDecimal(100)).intValue();

                //String url = "https://api.redadzukibeans.com/weChat/wxpay/otherRefundOrder?orderNo=" + retrieveOrder.getOrderId() + "&transactionId=" + userOrder.getTransactionId() + "&subMchId=" + dept.getSubMchId() + "&totalFee=" + userOrder.getPayment().multiply(new BigDecimal(100)).intValue() + "&refundFee=" + new BigDecimal(amount).multiply(new BigDecimal(100)).intValue();
                String result = HttpUtil.get(url);
            } else {
                //支付宝退款
                aliPayService.aliRefundOrder(userOrder.getTransactionId(), new BigDecimal(amount + ""), retrieveOrder.getOrderNo());

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
            //拒绝
            retrieveOrderService.update(Wrappers.<RetrieveOrder>lambdaUpdate()
                    .eq(RetrieveOrder::getOrderNo, reviewRefundOrder.getRetrieveOrderNo())
                    .set(RetrieveOrder::getStatus, 7));
        }
        reviewRefundOrder.setStatus(reviewStatus);
        reviewRefundOrder.setReviewRefundDesc(reviewRefundDesc);
        service.updateById(reviewRefundOrder);


        updateUserOrder.setId(Integer.parseInt(retrieveOrder.getOrderId()));
        updateUserOrder.setRefundReviewTime(LocalDateTime.now());
        userOrdertService.updateById(updateUserOrder);
        return RestResponse.ok();
    }

}
