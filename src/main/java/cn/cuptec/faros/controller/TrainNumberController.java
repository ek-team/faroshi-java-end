package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.service.TrainDataService;
import cn.cuptec.faros.service.TrainNumberMacAddService;
import cn.cuptec.faros.service.TrainNumberService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/trainNumber")
public class TrainNumberController extends AbstractBaseController<TrainNumberService, TrainNumber> {
    @Resource
    private TrainNumberMacAddService trainNumberMacAddService;

    @PostMapping("/update")
    public RestResponse save(@RequestBody TrainNumber trainNumber) {
        trainNumber.setId(1);
        service.updateById(trainNumber);
        String balanceMacAdd = trainNumber.getBalanceMacAdd();
        if (!StringUtils.isEmpty(balanceMacAdd)) {
            TrainNumberMacAdd trainNumberMacAdd = trainNumberMacAddService.getOne(new QueryWrapper<TrainNumberMacAdd>().lambda()
                    .eq(TrainNumberMacAdd::getMacAdd, balanceMacAdd));
            if (trainNumberMacAdd == null) {
                trainNumberMacAdd = new TrainNumberMacAdd();
            }
            trainNumberMacAdd.setTrainNumber(trainNumber.getBalanceTrainNumber());

            trainNumberMacAdd.setMacAdd(balanceMacAdd);
            trainNumberMacAddService.saveOrUpdate(trainNumberMacAdd);
        }
        String airTrainMacAdd = trainNumber.getAirTrainMacAdd();
        if (!StringUtils.isEmpty(airTrainMacAdd)) {
            TrainNumberMacAdd trainNumberMacAdd = trainNumberMacAddService.getOne(new QueryWrapper<TrainNumberMacAdd>().lambda()
                    .eq(TrainNumberMacAdd::getMacAdd, airTrainMacAdd));
            if (trainNumberMacAdd == null) {
                trainNumberMacAdd = new TrainNumberMacAdd();
            }
            trainNumberMacAdd.setTrainNumber(trainNumber.getAirTrainNumber());

            trainNumberMacAdd.setMacAdd(airTrainMacAdd);
            trainNumberMacAddService.saveOrUpdate(trainNumberMacAdd);
        }
        return RestResponse.ok();
    }

    @GetMapping("/get")
    public RestResponse get(@RequestParam("airTrainMacAdd") String airTrainMacAdd,
                            @RequestParam("balanceMacAdd") String balanceMacAdd) {
        List<String> macAdd = new ArrayList<>();
        macAdd.add(airTrainMacAdd);
        macAdd.add(balanceMacAdd);
        List<TrainNumberMacAdd> trainNumberMacAdds = trainNumberMacAddService.list(new QueryWrapper<TrainNumberMacAdd>().lambda()
                .in(TrainNumberMacAdd::getMacAdd, macAdd));
        Map<String, TrainNumberMacAdd> trainNumberMacAddMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(trainNumberMacAdds)) {
            trainNumberMacAddMap = trainNumberMacAdds.stream()
                    .collect(Collectors.toMap(TrainNumberMacAdd::getMacAdd, t -> t));
        }
        TrainNumber byId = service.getById(1);
        if (byId != null) {
            TrainNumberMacAdd trainNumberMacAdd = trainNumberMacAddMap.get(airTrainMacAdd);
            if (trainNumberMacAdd != null) {
                byId.setAirTrainNumber(trainNumberMacAdd.getTrainNumber());
            }else {
                byId.setAirTrainNumber(0);
            }
            TrainNumberMacAdd trainNumberMacAdd1 = trainNumberMacAddMap.get(balanceMacAdd);
            if (trainNumberMacAdd1 != null) {
                byId.setBalanceTrainNumber(trainNumberMacAdd1.getTrainNumber());
            }else {
                byId.setBalanceTrainNumber(0);
            }
        }
        return RestResponse.ok(byId);
    }

    @Override
    protected Class<TrainNumber> getEntityClass() {
        return TrainNumber.class;
    }
}
