package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
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
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 退款审核
 */
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
        retrieveOrderService.update(Wrappers.<RetrieveOrder>lambdaUpdate()
                .eq(RetrieveOrder::getOrderNo, reviewRefundOrder.getRetrieveOrderNo())
                .set(RetrieveOrder::getStatus, 6));
        return RestResponse.ok(service.save(reviewRefundOrder));
    }


    /**
     * 审核
     *
     * @param id
     * @param reviewStatus
     * @return
     */
    @GetMapping("/review")
    public RestResponse review(@RequestParam("id") Integer id, @RequestParam("reviewStatus") Integer reviewStatus) {
        ReviewRefundOrder reviewRefundOrder = service.getById(id);
        Double amount = reviewRefundOrder.getRefundFee().doubleValue();
        if (reviewStatus.equals(1)) {
            //退款
            RetrieveOrder retrieveOrder = retrieveOrderService.getOne(new QueryWrapper<RetrieveOrder>().lambda()
                    .eq(RetrieveOrder::getOrderNo, reviewRefundOrder.getRetrieveOrderNo()));
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
            String url = "https://api.redadzukibeans.com/weChat/wxpayother/otherRefundOrder?orderNo=" + retrieveOrder.getOrderNo() + "&transactionId=" + userOrder.getTransactionId() + "&subMchId=" + dept.getSubMchId() + "&totalFee=" + userOrder.getPayment().multiply(new BigDecimal(100)).intValue() + "&refundFee=" + new BigDecimal(amount).multiply(new BigDecimal(100)).intValue();

            //String url = "https://api.redadzukibeans.com/weChat/wxpay/otherRefundOrder?orderNo=" + retrieveOrder.getOrderId() + "&transactionId=" + userOrder.getTransactionId() + "&subMchId=" + dept.getSubMchId() + "&totalFee=" + userOrder.getPayment().multiply(new BigDecimal(100)).intValue() + "&refundFee=" + new BigDecimal(amount).multiply(new BigDecimal(100)).intValue();
            String result = HttpUtil.get(url);
            retrieveOrder.setStatus(4);
            retrieveOrderService.updateById(retrieveOrder);
        }
        if (reviewStatus.equals(2)) {
            //拒绝
            retrieveOrderService.update(Wrappers.<RetrieveOrder>lambdaUpdate()
                    .eq(RetrieveOrder::getOrderNo, reviewRefundOrder.getRetrieveOrderNo())
                    .set(RetrieveOrder::getStatus, 7));
        }
        reviewRefundOrder.setStatus(reviewStatus);
        service.updateById(reviewRefundOrder);
        return RestResponse.ok();
    }

}
