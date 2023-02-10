package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.entity.Bill;
import cn.cuptec.faros.entity.DeviceLog;
import cn.cuptec.faros.service.BillService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
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

    @PostMapping("/add")
    public RestResponse save(@RequestBody Bill bill) {
        bill.setUserId(SecurityUtils.getUser().getId());
        bill.setCreateTime(LocalDateTime.now());
        billService.save(bill);
        return RestResponse.ok();
    }

    @GetMapping("/getByOrderNo")
    public RestResponse getByOrderNo(@RequestParam("orderNo") String orderNo) {

        return RestResponse.ok(billService.getOne(new QueryWrapper<Bill>().lambda().eq(Bill::getOrderNo, orderNo)));
    }

    @PostMapping("/updateById")
    public RestResponse updateById(@RequestBody Bill bill) {

        billService.updateById(bill);
        return RestResponse.ok();
    }
}
