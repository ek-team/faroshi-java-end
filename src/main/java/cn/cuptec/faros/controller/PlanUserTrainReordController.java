package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.dto.GetTrainRecordDTO;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.service.*;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 训练记录
 */
@RestController
@RequestMapping("/planUserTrainRecord")
public class PlanUserTrainReordController extends AbstractBaseController<PlanUserTrainRecordService, TbUserTrainRecord> {
    @Resource
    private TrainDataService trainDataService;
    @Resource
    private SubPlanService subPlanService;
    @Resource
    private PlanUserService planUserService;

    @GetMapping("/pageByUid/{uid}")
    public RestResponse pageByUid(@PathVariable String uid) {

        Page<TbUserTrainRecord> page = getPage();

        return RestResponse.ok(service.pageByUid(page, uid));
    }

    @PostMapping("/save")
    public RestResponse<TbUserTrainRecord> saveAndData(@RequestBody List<TbUserTrainRecord> userTrainRecordList) {
        service.saveAndData(userTrainRecordList);
        return RestResponse.ok();
    }

    @GetMapping("/pageTrainRecordByXtUserId")
    public RestResponse pageTrainRecordByXtUserId(@RequestParam(value = "xtUserId", required = false) Integer xtUserId) {

        Page<TbUserTrainRecord> page = getPage();
        if (xtUserId == null) {
            xtUserId = SecurityUtils.getUser().getId();
        }
        IPage<TbUserTrainRecord> tbUserTrainRecordIPage = service.pageTrainRecordByXtUserId(page, xtUserId);
        if (tbUserTrainRecordIPage == null) {
            return RestResponse.failed("未查询到用户信息，请确认已关联用户信息");
        }
        return RestResponse.ok(tbUserTrainRecordIPage);
    }

    @GetMapping("/pageTrainRecordById")
    public RestResponse pageTrainRecordById(@RequestParam(value = "xtUserId", required = false) Integer xtUserId) {

        Page<TbUserTrainRecord> page = getPage();
        if (xtUserId == null) {
            xtUserId = SecurityUtils.getUser().getId();
        }
        IPage<TbUserTrainRecord> tbUserTrainRecordIPage = service.pageTrainRecordById(page, xtUserId);
        if (tbUserTrainRecordIPage == null) {
            return RestResponse.failed("未查询到用户信息，请确认已关联用户信息");
        }
        return RestResponse.ok(tbUserTrainRecordIPage);
    }

    /**
     * 根据手机号身份证号查询训练计划
     *
     * @return
     */
    @GetMapping("/trainRecordByPhone")
    public RestResponse trainRecordByPhone(@RequestParam(value = "phone", required = false) String phone, @RequestParam(value = "idCard", required = false) String idCard, @RequestParam(value = "xtUserId", required = false) String xtUserId) {

        return RestResponse.ok(service.trainRecordByPhone(phone, idCard,xtUserId));
    }

    /**
     * 查询用户每天每天的训练记录
     *
     * @return
     */

    @GetMapping("/getTrainStepCount")
    public RestResponse getTrainStepCount(@RequestParam(value = "userId", required = false) String userId) {
        List<TbUserTrainRecord> tbUserTrainRecords = service.listTrainRecordByUid(userId);
        if (CollectionUtils.isEmpty(tbUserTrainRecords)) {
            return RestResponse.ok();
        }

        Map<String, List<TbUserTrainRecord>> tbUserTrainRecordMap = tbUserTrainRecords.stream()
                .collect(Collectors.groupingBy(TbUserTrainRecord::getDateStr));
        List<TbUserTrainRecord> records = new ArrayList<>();
        for (String key : tbUserTrainRecordMap.keySet()) {

            List<TbUserTrainRecord> records1 = tbUserTrainRecordMap.get(key);
            int count = 0;
            if (!CollectionUtils.isEmpty(records1)) {
                count = records1.size();
            }
            TbUserTrainRecord record = new TbUserTrainRecord();
            record.setDateStr(key);
            record.setCount(count);
            records.add(record);
        }
        return RestResponse.ok(records);
    }

    /**
     * 查询用户每天的训练记录 计算每天踩踏最小值 和最大值
     *
     * @return
     */

    @GetMapping("/getTrainRecord")
    public RestResponse getTrainRecord(@RequestParam("idCard") String idCard) {
        List<TbTrainUser> list = planUserService.list(Wrappers.<TbTrainUser>lambdaQuery().eq(TbTrainUser::getIdCard, idCard));
        if (CollectionUtils.isEmpty(list)) {
            return RestResponse.ok(new ArrayList<>());
        }
        String userId = list.get(0).getUserId();
        List<TbUserTrainRecord> tbUserTrainRecords = service.listTrainRecordByUid(userId);
        if (CollectionUtils.isEmpty(tbUserTrainRecords)) {
            return RestResponse.ok();
        }
        List<Integer> tbUserTrainRecordIds = tbUserTrainRecords.stream().map(TbUserTrainRecord::getId)
                .collect(Collectors.toList());
        List<TbTrainData> tbTrainDatas = trainDataService.listByRecordIds(tbUserTrainRecordIds);//查询踩踏次数
        Map<Integer, List<TbTrainData>> tbTrainDataMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(tbTrainDatas)) {
            tbTrainDataMap = tbTrainDatas.stream()
                    .collect(Collectors.groupingBy(TbTrainData::getRecordId));
        }
        List<TbSubPlan> tbSubPlanList = subPlanService.list(Wrappers.<TbSubPlan>lambdaQuery().eq(TbSubPlan::getUserId, userId));//查询计划


        Map<String, List<TbUserTrainRecord>> tbUserTrainRecordMap = tbUserTrainRecords.stream()
                .collect(Collectors.groupingBy(TbUserTrainRecord::getDateStr));
        List<GetTrainRecordDTO> records = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");


        for (String key : tbUserTrainRecordMap.keySet()) {
            Integer planStepCount = 0;//当天计划踩踏次数
            if (!CollectionUtils.isEmpty(tbSubPlanList)) {
                for (TbSubPlan tbSubPlan : tbSubPlanList) {
                    try {
                        Date dateStr = sdf.parse(key + " 00:00:00");
                        Date startDate = tbSubPlan.getStartDate();
                        Date endDate = tbSubPlan.getEndDate();
                        if (dateStr.before(endDate) && dateStr.after(startDate)) {
                            int trainStep = tbSubPlan.getTrainStep();
                            if (trainStep > 0) {
                                planStepCount = trainStep / 7;
                            }
                            break;
                        }
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }


                }
            }
            GetTrainRecordDTO getTrainRecordDTO = new GetTrainRecordDTO();
            List<TbUserTrainRecord> records1 = tbUserTrainRecordMap.get(key);//每天的训练记录
            int maxTargetLoad = 0;
            int miniTargetLoad = Integer.MAX_VALUE;
            int totalStepCount = 0;//总踩踏次数
            int totalTargetLoad = 0;//总负重次数
            if (!CollectionUtils.isEmpty(records1)) {
                for (TbUserTrainRecord tbUserTrainRecord : records1) {


                    List<TbTrainData> tbTrainData = tbTrainDataMap.get(tbUserTrainRecord.getId());
                    if (!CollectionUtils.isEmpty(tbTrainData)) {
                        totalStepCount = totalStepCount + tbTrainData.size();
                        for (TbTrainData tbTrainData1 : tbTrainData) {
                            totalTargetLoad = totalTargetLoad + tbTrainData1.getRealLoad();
                            if (tbTrainData1.getRealLoad() > maxTargetLoad) {
                                maxTargetLoad = tbTrainData1.getRealLoad();
                            }
                            if (tbTrainData1.getRealLoad() < miniTargetLoad) {
                                miniTargetLoad = tbTrainData1.getRealLoad();
                            }
                        }
                    }

                }
            }
            getTrainRecordDTO.setTbUserTrainRecordList(records1);
            getTrainRecordDTO.setDateStr(key);
            getTrainRecordDTO.setMaxTargetLoad(maxTargetLoad);
            getTrainRecordDTO.setMiniTargetLoad(miniTargetLoad);
            if(totalTargetLoad!=0 && totalStepCount!=0 ){
                getTrainRecordDTO.setAverageTargetLoad(totalTargetLoad / totalStepCount);
            }
            getTrainRecordDTO.setTotalStepCount(totalStepCount);
            getTrainRecordDTO.setPlanStepCount(planStepCount);
            records.add(getTrainRecordDTO);
        }
        Collections.sort(records);
        return RestResponse.ok(records);
    }

    @Override
    protected Class<TbUserTrainRecord> getEntityClass() {
        return TbUserTrainRecord.class;
    }
}
