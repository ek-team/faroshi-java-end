package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.DataArea;

import cn.cuptec.faros.entity.TimeData;
import cn.cuptec.faros.service.DataAreaService;
import cn.cuptec.faros.service.TimeDataService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dataArea")
public class DataAreaController extends AbstractBaseController<DataAreaService, DataArea> {
    @Resource
    private TimeDataService timeDataService;

    @PostMapping("/save")
    public RestResponse<Boolean> save(@RequestBody List<DataArea> dataArea) {
        service.remove(new QueryWrapper<DataArea>().lambda().eq(DataArea::getType, dataArea.get(0).getType()));
        service.saveBatch(dataArea);
        for (DataArea dataArea1 : dataArea) {
            List<TimeData> timeData = dataArea1.getTimeData();
            if (!CollectionUtils.isEmpty(timeData)) {
                for (TimeData timeData1 : timeData) {
                    timeData1.setDataAreaId(dataArea1.getId());
                }
                timeDataService.saveBatch(timeData);
            }
        }
        return RestResponse.ok();
    }

    @GetMapping("/get")
    public RestResponse get(@RequestParam("type") Integer type) {
        List<DataArea> dataAreas = service.list(new QueryWrapper<DataArea>().lambda().eq(DataArea::getType, type));
        if (!CollectionUtils.isEmpty(dataAreas)) {
            List<Integer> ids = dataAreas.stream().map(DataArea::getId)
                    .collect(Collectors.toList());
            List<TimeData> list = timeDataService.list(new QueryWrapper<TimeData>().lambda().in(TimeData::getDataAreaId, ids));
            if (!CollectionUtils.isEmpty(list)) {
                Map<Integer, List<TimeData>> map = list.stream()
                        .collect(Collectors.groupingBy(TimeData::getDataAreaId));
                for (DataArea dataArea : dataAreas) {
                    dataArea.setTimeData(map.get(dataArea.getId()));
                }
            }
        }
        return RestResponse.ok(dataAreas);
    }

    @Override
    protected Class<DataArea> getEntityClass() {
        return DataArea.class;
    }
}
