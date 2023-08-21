package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.service.MacAddOrderCountService;
import cn.cuptec.faros.service.RehabilitationService;
import cn.cuptec.faros.service.ReportRecordService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 设备订单数
 */
@Slf4j
@RestController
@RequestMapping("/rehabilitation")
public class RehabilitationController extends AbstractBaseController<RehabilitationService, Rehabilitation> {
    @Resource
    private MacAddOrderCountService macAddOrderCountService;

    @PostMapping("/update")
    public RestResponse save(@RequestBody Rehabilitation rehabilitation) {
        rehabilitation.setId(1);
        service.updateById(rehabilitation);
        String balanceMacAdd = rehabilitation.getBalanceMacAdd();
        if (!StringUtils.isEmpty(balanceMacAdd)) {
            MacAddOrderCount macAddOrderCount = macAddOrderCountService.getOne(new QueryWrapper<MacAddOrderCount>().lambda()
                    .eq(MacAddOrderCount::getMacAdd, balanceMacAdd));
            if (macAddOrderCount == null) {
                macAddOrderCount = new MacAddOrderCount();
                macAddOrderCount.setCount(rehabilitation.getBalanceTrainOrder());
            } else {
                macAddOrderCount.setCount(macAddOrderCount.getCount() + rehabilitation.getBalanceTrainOrder());
            }
            macAddOrderCount.setMacAdd(balanceMacAdd);
            macAddOrderCountService.saveOrUpdate(macAddOrderCount);
        }
        String airTrainMacAdd = rehabilitation.getAirTrainMacAdd();
        if (!StringUtils.isEmpty(airTrainMacAdd)) {
            MacAddOrderCount macAddOrderCount1 = macAddOrderCountService.getOne(new QueryWrapper<MacAddOrderCount>().lambda()
                    .eq(MacAddOrderCount::getMacAdd, airTrainMacAdd));
            if (macAddOrderCount1 == null) {
                macAddOrderCount1 = new MacAddOrderCount();
                macAddOrderCount1.setCount(rehabilitation.getAirTrainOrder());
            } else {
                macAddOrderCount1.setCount(macAddOrderCount1.getCount() + rehabilitation.getAirTrainOrder());
            }
            macAddOrderCount1.setMacAdd(airTrainMacAdd);
            macAddOrderCountService.saveOrUpdate(macAddOrderCount1);
        }
        return RestResponse.ok();
    }

    @GetMapping("/get")
    public RestResponse get(@RequestParam("airTrainMacAdd") String airTrainMacAdd,
                            @RequestParam("balanceMacAdd") String balanceMacAdd) {
        List<String> macAdd = new ArrayList<>();
        macAdd.add(airTrainMacAdd);
        macAdd.add(balanceMacAdd);
        List<MacAddOrderCount> macAddOrderCounts = macAddOrderCountService.list(new QueryWrapper<MacAddOrderCount>().lambda()
                .in(MacAddOrderCount::getMacAdd, macAdd));
        Map<String, MacAddOrderCount> macAddOrderCountHashMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(macAddOrderCounts)) {
            macAddOrderCountHashMap = macAddOrderCounts.stream()
                    .collect(Collectors.toMap(MacAddOrderCount::getMacAdd, t -> t));
        }
        Rehabilitation byId = service.getById(1);
        if (byId != null) {
            MacAddOrderCount macAddOrderCount = macAddOrderCountHashMap.get(airTrainMacAdd);
            if (macAddOrderCount != null) {
                byId.setAirTrainOrder(macAddOrderCount.getCount());
            }
            MacAddOrderCount macAddOrderCount1 = macAddOrderCountHashMap.get(balanceMacAdd);
            if (macAddOrderCount1 != null) {
                byId.setBalanceTrainOrder(macAddOrderCount1.getCount());
            }
        }
        return RestResponse.ok(byId);
    }

    @Override
    protected Class<Rehabilitation> getEntityClass() {
        return Rehabilitation.class;
    }
}
