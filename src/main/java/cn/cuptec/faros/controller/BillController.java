package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.entity.Bill;
import cn.cuptec.faros.entity.ChatUser;
import cn.cuptec.faros.entity.DeviceLog;
import cn.cuptec.faros.entity.UserOrder;
import cn.cuptec.faros.service.BillService;
import cn.cuptec.faros.service.UserOrdertService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;

/**
 * 用户填写发票信息
 */
@RestController
@AllArgsConstructor
@RequestMapping("/bill")
public class BillController {
    @Resource
    private BillService billService;
    @Resource
    private UserOrdertService userOrdertService;

    @PostMapping("/add")
    public RestResponse save(@RequestBody Bill bill) {
        bill.setUserId(SecurityUtils.getUser().getId());
        bill.setCreateTime(LocalDateTime.now());
        billService.save(bill);
        UserOrder userOrder = new UserOrder();
        userOrder.setOrderNo(bill.getOrderNo());
        userOrder.setBillId(bill.getId());
        String orderNo = bill.getOrderNo();
        String[] split = orderNo.split("-");
        orderNo = split[1];
        userOrdertService.update(Wrappers.<UserOrder>lambdaUpdate()
                .eq(UserOrder::getOrderNo, orderNo)
                .set(UserOrder::getBillId, bill.getId()));
        return RestResponse.ok();
    }

    @GetMapping("/getByOrderNo")
    public RestResponse getByOrderNo(@RequestParam("orderNo") String orderNo) {

        return RestResponse.ok(billService.getOne(new QueryWrapper<Bill>().lambda().eq(Bill::getOrderNo, orderNo)));
    }

    @GetMapping("/getById")
    public RestResponse getById(@RequestParam("id") String id) {

        return RestResponse.ok(billService.getById(id));
    }

    @PostMapping("/updateById")
    public RestResponse updateById(@RequestBody Bill bill) {

        billService.updateById(bill);
        return RestResponse.ok();
    }
}
