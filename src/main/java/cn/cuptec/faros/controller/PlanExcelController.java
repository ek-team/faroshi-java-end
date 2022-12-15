package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.common.utils.MapperUtil;
import cn.cuptec.faros.entity.PlanExcelBO;
import cn.cuptec.faros.entity.TbPlan;
import cn.cuptec.faros.entity.TbTrainData;
import cn.cuptec.faros.entity.TbUserTrainRecord;
import cn.cuptec.faros.service.PlanUserTrainRecordService;
import cn.cuptec.faros.service.TrainDataService;
import cn.cuptec.faros.util.ExcelUtil;
import org.apache.commons.collections4.ListUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/plan/excel")
public class PlanExcelController {
    @Resource
    private PlanUserTrainRecordService planUserTrainRecordService;
    @Resource
    private TrainDataService trainDataService;

    @GetMapping("export")
    public RestResponse listexportByUid(HttpServletResponse response, @RequestParam("xtUserId") Integer xtUserId, @RequestParam("name") String name) {
        List<TbUserTrainRecord> tbUserTrainRecords = planUserTrainRecordService.listTrainRecordByXtUserId(xtUserId);
        if (CollectionUtils.isEmpty(tbUserTrainRecords)) {
            return RestResponse.failed("没有数据");
        }
        List<PlanExcelBO> planExcelBOS = new ArrayList<>();

        MapperUtil.populateList(tbUserTrainRecords, planExcelBOS, PlanExcelBO.class);
        //  查询每一天的详细信息

        List<Integer> recordIds = tbUserTrainRecords.stream().map(TbUserTrainRecord::getId)
                .collect(Collectors.toList());
        List<TbTrainData> tbTrainData = trainDataService.listByRecordIds(recordIds);
        List<PlanExcelBO> planExcelBOS1 = new ArrayList<>();
        if (!CollectionUtils.isEmpty(tbTrainData)) {

            Map<Integer, List<TbTrainData>> tbTrainDataMap = tbTrainData.stream()
                    .collect(Collectors.groupingBy(TbTrainData::getRecordId));

            for (PlanExcelBO planExcelBO : planExcelBOS) {

                List<TbTrainData> tbTrainData1 = tbTrainDataMap.get(planExcelBO.getId());
                if (!CollectionUtils.isEmpty(tbTrainData1)) {
                    planExcelBO.setSize(tbTrainData1.size());
                    planExcelBO.setName(name);
                    planExcelBOS1.add(planExcelBO);
                    for (TbTrainData tbTrainData2 : tbTrainData1) {
                        PlanExcelBO planExcelBO1 = new PlanExcelBO();
                        planExcelBO1.setRealLoad(tbTrainData2.getRealLoad());
                        planExcelBO1.setFrequencyDetail(tbTrainData2.getFrequency());
                        planExcelBO1.setTargetLoadDetail(tbTrainData2.getTargetLoad());
                        planExcelBOS1.add(planExcelBO1);
                    }
                }


            }
        } else {
            planExcelBOS1.addAll(planExcelBOS);
        }
        String cFileName = null;
        String cFileName1 = null;
        String cFileName2 = null;
        try {
            cFileName = URLEncoder.encode("planrecord", "UTF-8");
            cFileName1 = URLEncoder.encode("planrecord1", "UTF-8");
            cFileName2 = URLEncoder.encode("planrecord1", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        try {

            ExcelUtil.writeExcel(response, planExcelBOS1, cFileName, "训练记录1", PlanExcelBO.class);


        } catch (Exception e) {
            e.printStackTrace();
        }
        return RestResponse.ok();
    }
}
