package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.common.utils.MapperUtil;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.service.EvaluationRecordsService;
import cn.cuptec.faros.service.PlanUserService;
import cn.cuptec.faros.service.PlanUserTrainRecordService;
import cn.cuptec.faros.service.TrainDataService;
import cn.cuptec.faros.util.ExcelUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.apache.commons.collections4.ListUtils;
import org.springframework.beans.BeanUtils;
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
    @Resource
    private PlanUserService planUserService;
    @Resource
    private EvaluationRecordsService evaluationRecordsService;

    @GetMapping("export")
    public RestResponse listexportByUid(HttpServletResponse response, @RequestParam("xtUserId") Integer xtUserId, @RequestParam("name") String name) {
        List<PlanUserExcelBO> planExcelBOS0 = new ArrayList<>();
        TbTrainUser infoByUXtUserId = planUserService.getOne(Wrappers.<TbTrainUser>lambdaQuery().eq(TbTrainUser::getId, xtUserId));//getInfoByUXtUserId(xtUserId);
        PlanUserExcelBO planUserExcelBO = new PlanUserExcelBO();
        planUserExcelBO.setName(infoByUXtUserId.getName());
        planUserExcelBO.setDiseaseDiagnosis(infoByUXtUserId.getDiseaseDiagnosis());
        planUserExcelBO.setHeight(infoByUXtUserId.getHeight());
        planUserExcelBO.setEducationLevel(infoByUXtUserId.getEducationLevel());
        planUserExcelBO.setId(infoByUXtUserId.getId());
        planUserExcelBO.setIdCard(infoByUXtUserId.getIdCard());
        planUserExcelBO.setOnsetDiagnosis(infoByUXtUserId.getOnsetDiagnosis());
        planUserExcelBO.setDiagnosis(infoByUXtUserId.getDiagnosis());
        planUserExcelBO.setOnsetTime(infoByUXtUserId.getOnsetTime());
        planUserExcelBO.setHospitalAddress(infoByUXtUserId.getHospitalAddress());
        planUserExcelBO.setHospitalName(infoByUXtUserId.getHospitalName());
        planExcelBOS0.add(planUserExcelBO);


        List<PlanExcelBO> planExcelBOS1 = new ArrayList<>();

        List<EvaluationRecordsExcelBo> planExcelBOS2 = new ArrayList<>();
        //训练记录
        List<EvaluationRecords> evaluationRecordsList = evaluationRecordsService.list(new QueryWrapper<EvaluationRecords>().lambda().eq(EvaluationRecords::getUserId, infoByUXtUserId.getUserId()));
        if (!CollectionUtils.isEmpty(evaluationRecordsList)) {

            for (EvaluationRecords evaluationRecords : evaluationRecordsList) {
                EvaluationRecordsExcelBo evaluationRecordsExcelBo = new EvaluationRecordsExcelBo();
                evaluationRecordsExcelBo.setEvaluateResult(evaluationRecords.getEvaluateResult());
                evaluationRecordsExcelBo.setCreateDate(evaluationRecords.getCreateDate());
                evaluationRecordsExcelBo.setFifthValue(evaluationRecords.getFifthValue());
                evaluationRecordsExcelBo.setFirstValue(evaluationRecords.getFirstValue());
                evaluationRecordsExcelBo.setFourthValue(evaluationRecords.getFourthValue());
                evaluationRecordsExcelBo.setSecondValue(evaluationRecords.getSecondValue());
                evaluationRecordsExcelBo.setThirdValue(evaluationRecords.getThirdValue());
                evaluationRecordsExcelBo.setVas(evaluationRecords.getVas());
                evaluationRecordsExcelBo.setUpdateDate(evaluationRecords.getUpdateDate());

                planExcelBOS2.add(evaluationRecordsExcelBo);
            }

        }


        List<TbUserTrainRecord> tbUserTrainRecords = planUserTrainRecordService.listTrainRecordByXtUserId(xtUserId);

        List<PlanExcelBO> planExcelBOS = new ArrayList<>();

        MapperUtil.populateList(tbUserTrainRecords, planExcelBOS, PlanExcelBO.class);
        //  查询每一天的详细信息

        List<Integer> recordIds = tbUserTrainRecords.stream().map(TbUserTrainRecord::getId)
                .collect(Collectors.toList());
        if(!CollectionUtils.isEmpty(recordIds)){
            List<TbTrainData> tbTrainData = trainDataService.listByRecordIds(recordIds);
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

            ExcelUtil.writeExcel(response, planExcelBOS0, planExcelBOS1, planExcelBOS2, cFileName, "训练记录1", PlanExcelBO.class);


        } catch (Exception e) {
            e.printStackTrace();
        }
        return RestResponse.ok();
    }
}
