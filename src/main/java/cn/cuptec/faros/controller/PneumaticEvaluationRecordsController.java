package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.service.PneumaticEvaluationRecordsAirListService;
import cn.cuptec.faros.service.PneumaticEvaluationRecordsService;
import cn.cuptec.faros.service.PneumaticPlanService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 气动评估记录
 */
@RestController
@RequestMapping("/pneumaticEvaluationRecords")
@Slf4j
public class PneumaticEvaluationRecordsController extends AbstractBaseController<PneumaticEvaluationRecordsService, PneumaticEvaluationRecords> {
    @Resource
    private PneumaticEvaluationRecordsAirListService pneumaticEvaluationRecordsAirListService;

    /**
     * 上传评估记录
     */
    @PostMapping("/addBatch")
    public RestResponse addEvaluationRecords(@RequestBody List<PneumaticEvaluationRecords> list) {


        for (PneumaticEvaluationRecords pneumaticEvaluationRecords : list) {
            pneumaticEvaluationRecords.setCreateTime(LocalDateTime.now());
            List<PneumaticEvaluationRecordsAirList> lists = new ArrayList<>();
            List<String> airList = pneumaticEvaluationRecords.getAirList();
            if (!CollectionUtils.isEmpty(airList)) {
                for (String air : airList) {
                    PneumaticEvaluationRecordsAirList airList1 = new PneumaticEvaluationRecordsAirList();
                    airList1.setData(air);
                    airList1.setKeyId(pneumaticEvaluationRecords.getKeyId());
                    lists.add(airList1);
                }
                pneumaticEvaluationRecordsAirListService.saveBatch(lists);
            }
            if (pneumaticEvaluationRecords.getHealthyHand() == 0) {
                service.remove(new QueryWrapper<PneumaticEvaluationRecords>().lambda()
                        .eq(PneumaticEvaluationRecords::getGrade, pneumaticEvaluationRecords.getGrade())
                        .eq(PneumaticEvaluationRecords::getHandMovement, pneumaticEvaluationRecords.getHandMovement())
                        .eq(PneumaticEvaluationRecords::getHealthyHand, pneumaticEvaluationRecords.getHealthyHand()));
            } else {
                service.remove(new QueryWrapper<PneumaticEvaluationRecords>().lambda().
                        eq(PneumaticEvaluationRecords::getUpdateTime, pneumaticEvaluationRecords.getUpdateTime())
                        .eq(PneumaticEvaluationRecords::getGrade, pneumaticEvaluationRecords.getGrade())
                        .eq(PneumaticEvaluationRecords::getHandMovement, pneumaticEvaluationRecords.getHandMovement())
                        .eq(PneumaticEvaluationRecords::getHealthyHand, pneumaticEvaluationRecords.getHealthyHand()));
            }


        }

        service.saveBatch(list);
        return RestResponse.ok();
    }

    /**
     * 查询评估记录
     */
    @GetMapping("/getRecordsByUserId")
    public RestResponse getRecordsByUserId(@RequestParam("userId") String userId, @RequestParam(value = "grade", required = false) Integer grade) {
        List<PneumaticEvaluationRecords> list = new ArrayList<>();
        if (grade == null) {
            list = service.list(new QueryWrapper<PneumaticEvaluationRecords>().lambda().eq(PneumaticEvaluationRecords::getUserId, userId));

        } else {
            list = service.list(new QueryWrapper<PneumaticEvaluationRecords>().lambda().eq(PneumaticEvaluationRecords::getUserId, userId).eq(PneumaticEvaluationRecords::getGrade, grade));

        }

        if (!CollectionUtils.isEmpty(list)) {
            List<String> keyIds = list.stream().map(PneumaticEvaluationRecords::getKeyId)
                    .collect(Collectors.toList());
            List<PneumaticEvaluationRecordsAirList> airLists = pneumaticEvaluationRecordsAirListService.list(Wrappers.<PneumaticEvaluationRecordsAirList>lambdaQuery().in(PneumaticEvaluationRecordsAirList::getKeyId, keyIds));
            if (!CollectionUtils.isEmpty(airLists)) {
                Map<String, List<PneumaticEvaluationRecordsAirList>> map = airLists.stream()
                        .collect(Collectors.groupingBy(PneumaticEvaluationRecordsAirList::getKeyId));
                for (PneumaticEvaluationRecords records : list) {
                    List<PneumaticEvaluationRecordsAirList> lists = map.get(records.getKeyId());
                    records.setAirLists(lists);
                }

            }
        }
        Collections.sort(list);
        return RestResponse.ok(list);
    }

    /**
     * 查询评估记录
     */
    @GetMapping("/deleteRecordsByUserId")
    public RestResponse deleteRecordsByUserId(@RequestParam("userId") String userId, @RequestParam("keyId") String keyId) {

        service.remove(new QueryWrapper<PneumaticEvaluationRecords>().lambda().eq(PneumaticEvaluationRecords::getUserId, userId)
                .eq(PneumaticEvaluationRecords::getKeyId, keyId));
        return RestResponse.ok();
    }

    @Override
    protected Class<PneumaticEvaluationRecords> getEntityClass() {
        return PneumaticEvaluationRecords.class;
    }
}
