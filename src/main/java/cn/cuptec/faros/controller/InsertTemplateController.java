package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.entity.Global;
import cn.cuptec.faros.entity.TbPlan;
import cn.cuptec.faros.entity.TbSubPlan;
import cn.cuptec.faros.service.PlanService;
import cn.cuptec.faros.service.SubPlanService;

import cn.cuptec.faros.util.DateFormatUtil;
import cn.cuptec.faros.util.SnowflakeIdUtil;
import cn.hutool.http.HttpUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/insertTemplate")
public class InsertTemplateController {
    @Resource
    private PlanService planService;
    @Resource
    private SubPlanService subPlanService;

    private static final int trainCountMinute = 10;//每分钟训练步数
    private static final long dayMs = 24 * 60 * 60 * 1000;
    private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    @GetMapping("/insert")
    public RestResponse insertTemplate(@RequestParam("saveResult") int saveResult,
                                       @RequestParam("date") String date,
                                       @RequestParam("weight") String weight,
                                       @RequestParam("userId") long userId,
                                       @RequestParam("planStartTime") long planStartTime,
                                       @RequestParam("diagnosis") String diagnosis,
                                       @RequestParam("startStep") int startStep,
                                       @RequestParam("treatmentMethodId") String treatmentMethodId
    ) {
//        int diagType = getDiagnosticNum(diagnosis);
        int diagType = getPlanNum(treatmentMethodId);
        String planFinishLoad = weight;
        List<TbSubPlan> list = subPlanService.list(new QueryWrapper<TbSubPlan>().lambda().eq(TbSubPlan::getUserId, userId));
        if(!CollectionUtils.isEmpty(list)){
            return RestResponse.failed("该用户已有计划");
        }
//        saveResult = getCalcValue(saveResult);
        int value = 0;
        String startDate;
        String finishDate;
        switch (diagType){
            case 0:
                break;
            case 1://全髋关节置换  术后第一天开始负重，逐步负重,6 周内达完全负重
                value = generatePlan1(saveResult,date,planFinishLoad,startStep,planStartTime, userId);
                break;
            case 2://全膝关节置换 术后第一天开始负重，逐步负重,6 周内达完全负重
                value = generatePlan2(saveResult,date,planFinishLoad,startStep,planStartTime, userId);
                break;
            case 3://股骨颈骨折
            case 4://股骨转子间骨折 1周时为健侧 51%，逐步增加,12周时为健侧 87%，直至 100%
                value = generatePlan3(saveResult,date,planFinishLoad,startStep,planStartTime, userId);
                break;
            case 5://胫骨平台骨折（钢板固定）6周时20kg，逐步增加，16周左右达到健侧100%
                value = generatePlan4(saveResult,date,planFinishLoad,startStep,planStartTime, userId);
                break;
            case 6://胫骨平台骨折（钢板内固定）2周为一个周期，逐步由起始重量增加至完全负重，总周期39周
                value = generatePlan5(saveResult,date,planFinishLoad,startStep,planStartTime, userId);
                break;
            case 7://胫骨中段骨折（石膏固定）2周为一个周期，逐步由起始重量增加至完全负重，总周期24周
                value = generatePlan6(saveResult,date,planFinishLoad,startStep,planStartTime, userId);
                break;
            case 8://胫骨中段骨折（髓内钉）
            case 9://胫骨中段骨折（桥接钢板）2周为一个周期，逐步由起始重量增加至完全负重，总周期24周
                value = generatePlan7(saveResult,date,planFinishLoad,startStep,planStartTime, userId);
                break;
            case 10://胫骨远端骨折  髋关节截骨术  1到5周训练5到10kg，6到12周达到体重的50%
                value = generatePlanJieGu(saveResult,date,planFinishLoad,startStep,planStartTime, userId);
                break;
            case 11://踝关节骨折（钢板内固定）术后两天开始训练，逐渐 16 周后达到完全负重
                value = generatePlan8(saveResult,date,planFinishLoad,startStep,planStartTime, userId);
                break;
            case 12://跟骨骨折（钢板固定）手术四周后开始负重训练，8周内10kg，10周20kg，12周40kg，直至完全负重
                value = generatePlan9(saveResult,date,planFinishLoad,startStep,planStartTime, userId);
                break;
            case 13://踝关节韧带损伤（踝关节韧带重建术）5公斤开始，逐步负重直至完全负重
                value = generatePlan10(saveResult,date,planFinishLoad,startStep,planStartTime, userId);
                break;
            case 14://股骨头坏死（腓骨移植术）术后七周达到12公斤，每2周增加5公斤，直到完全负重
                value = generatePlan11(saveResult,date,planFinishLoad,startStep,planStartTime, userId);
                break;
            case 15://四个月的默认计划
                startDate = date;
                finishDate = DateFormatUtil.getBeforeOrAfterDate(4*30,startDate);
                value =  generateDefaultPlan(startDate,userId,String.valueOf(saveResult),finishDate,planFinishLoad,planStartTime,startStep);
                break;
            case 16://六个月的默认计划
                startDate = date;
                finishDate = DateFormatUtil.getBeforeOrAfterDate(6*30,startDate);
                value = generateDefaultPlan(startDate,userId,String.valueOf(saveResult),finishDate,planFinishLoad,planStartTime,startStep);
                break;
            case 17://八周的默认计划
                startDate = date;
                finishDate = DateFormatUtil.getBeforeOrAfterDate(8*7,startDate);
                value = generateDefaultPlan(startDate,userId,String.valueOf(saveResult),finishDate,planFinishLoad,planStartTime,startStep);
                break;
            case 18://六周的默认计划
                startDate = date;
                finishDate = DateFormatUtil.getBeforeOrAfterDate(6*7,startDate);
                value =  generateDefaultPlan(startDate,userId,String.valueOf(saveResult),finishDate,planFinishLoad,planStartTime,startStep);
                break;
            default:
                return RestResponse.failed("当前病种没有计划");
        }

//        String url = "nsertTemplate/insert?saveResult="+saveResult+"&date="+date+"&weight="+weight+"&userId="+userId+"&diagnosis"+diagnosis;
//        String post = HttpUtil.get(url);
        return RestResponse.ok(value);
    }
    public int generateDefaultPlan(String startDate,long userId, String startLoad, String endDate, String endLoad,long planStartTime,int initStep){
        List<TbSubPlan> subPlanEntityList = new ArrayList<>();
        endDate = DateFormatUtil.increaseOneDayOneSecondLess(0,endDate);
        long startTime = DateFormatUtil.getString2Date(startDate);
        long endTime = DateFormatUtil.getString2Date(endDate);
        int weekCount = (int) Math.ceil(((endTime-startTime) / (7.0 * dayMs)));
        if (weekCount == 0 || weekCount == 1)
            weekCount = 2;
        TbPlan planEntity = insert(Integer.parseInt(startLoad),userId,endLoad,1,startDate,endDate,Global.TrainTime,weekCount*7);
        insert(planEntity);
        subPlanEntityList.addAll(insert5Test(startDate,userId,Integer.parseInt(endLoad),Integer.parseInt(startLoad),1,weekCount));
        List<TbSubPlan> modifySubPlanList = modifySubPlanData(subPlanEntityList,Global.TrainTime, initStep,Global.MaxTrainStep);
        insertMany(modifyStartEndDate(planStartTime,modifySubPlanList));
        return 0;
    }

    public List<TbSubPlan> insertJieGuTest(String startDate, long userId, int desWeight, int loadWeight, int classId, int weekTotal) {
        List<TbSubPlan> subPlanEntityList = new ArrayList<>();
//        if (weekTotal/2 == 1)
//            weekTotal = weekTotal + 1;
        for (int i = 0; i < weekTotal; i++) {
            TbSubPlan subPlanEntity = new TbSubPlan();
            subPlanEntity.setKeyId(SnowflakeIdUtil.getUniqueId());
            subPlanEntity.setClassId(classId);
            subPlanEntity.setUserId(userId);
            if (weekTotal == 1) {
                subPlanEntity.setLoad(loadWeight + (desWeight - loadWeight) * i);
            } else {
                subPlanEntity.setLoad(loadWeight + (desWeight - loadWeight) * i / (weekTotal - 1));
            }
            subPlanEntity.setWeekNum(i + 1);
            subPlanEntity.setDayNum(0);
            subPlanEntity.setPlanStatus(0);
            subPlanEntity.setStartDateStr(DateFormatUtil.getBeforeOrAfterDate(i * 7, startDate));
            if (i == weekTotal - 1) {
                subPlanEntity.setEndDateStr(DateFormatUtil.increaseOneDayOneSecondLess((i + 1) * 7 - 1, startDate));
            } else {
                subPlanEntity.setEndDateStr(DateFormatUtil.getBeforeOrAfterDate((i + 1) * 7, startDate));
            }
            subPlanEntityList.add(subPlanEntity);
        }
        return subPlanEntityList;
    }

    public List<TbSubPlan> insertListJieGuTest(int loadWeight, String date, String weightStr, long userId) {//髋关节截骨术
        String startDate = date;
        List<TbSubPlan> subPlanEntityList = new ArrayList<>();
        loadWeight = 5;
        String endDate = DateFormatUtil.increaseOneDayOneSecondLess(5 * 7 - 1, startDate);
//        insert(loadWeight,1,startDate,endDate,Global.TrainTime,5*7);
        subPlanEntityList.addAll(insertJieGuTest(startDate, userId, 10, loadWeight, 1, 5));
        String classTwoStartDate = DateFormatUtil.getString2DateIncreaseOneDay(endDate, null);
//        insert(10,2,classTwoStartDate,DateFormatUtil.increaseOneDayOneSecondLess(7*7-1,classTwoStartDate),Global.TrainTime,7*7);
        int weight = Integer.parseInt(weightStr);
        subPlanEntityList.addAll(insertJieGuTest(classTwoStartDate, userId, weight / 2, 10, 2, 7));
        return subPlanEntityList;
    }

    public int generatePlanJieGu(int targetLoad, String date, String weightStr,int startStep,long planStartTime, long userId) {
        List<TbSubPlan> subPlanEntityList = new ArrayList<>();
        String startDate = date;
        targetLoad = 5;
        String endDate = DateFormatUtil.increaseOneDayOneSecondLess(5 * 7 - 1, startDate);
        TbPlan planEntity = insert(targetLoad, userId, weightStr, 1, startDate, endDate, Global.TrainTime, 5 * 7);
        String classTwoStartDate = DateFormatUtil.getString2DateIncreaseOneDay(endDate, null);
        TbPlan planEntity1 = insert(10, userId, weightStr, 2, classTwoStartDate, DateFormatUtil.increaseOneDayOneSecondLess(7 * 7 - 1, classTwoStartDate), Global.TrainTime, 7 * 7);
        insert(planEntity);
        insert(planEntity1);
        subPlanEntityList.addAll(insertListJieGuTest(targetLoad, date, weightStr, userId));
        List<TbSubPlan> modifySubPlanList = modifySubPlanData(subPlanEntityList, Global.TrainTime, startStep, Global.MaxTrainStep);
        List<TbSubPlan> subPlanEntities1 = modifyStartEndDate(planStartTime, modifySubPlanList);
        insertMany(subPlanEntities1);
        if (subPlanEntities1.size() <= 0) {
//            TrainPlanManager.getInstance().clearTrainPlanDatabaseByUserId(userId);
            return Integer.MAX_VALUE;
        } else {
            return 0;
        }

    }

    public List<TbSubPlan> insertList3Test(String date, int loadWeight, String weightStr, long userId) {//股骨近端骨折 转子间骨折
        List<TbSubPlan> subPlanEntityList = new ArrayList<>();
        String startDate = date;
        int weight = Integer.parseInt(weightStr);
        int newLoadWeight1 = (int) (weight * 0.87);
        int newLoadWeight2 = (int) (weight * 0.51);
        float weekDiff = (float) (weight * (0.87 - 0.51)) / 11;
        int weekCount = (int) Math.ceil((weight * (1 - 0.87) / weekDiff));//推算剩余的负重对应结束的时间
        if (weekCount == 0) {
            weekCount = 1;
        }
//        if (targetLoad <= newLoadWeight2){
//            subPlanEntityList.addAll(SubPlanManager.getInstance().insert3Test(startDate, newLoadWeight2, loadWeight, 1, 1));
//            return subPlanEntityList;
//        }
        if (loadWeight > newLoadWeight1) {
//            insert(loadWeight, 1, startDate, DateFormatUtil.increaseOneDayOneSecondLess(weekCount * 7-1, startDate), Global.TrainTime, weekCount);
            subPlanEntityList.addAll(insert3Test(startDate, userId, weight, loadWeight, 1, weekCount));
        } else if (loadWeight > newLoadWeight2) {
            int weekNumer = (int) Math.ceil((weight * 0.87 - loadWeight) / weekDiff);//推算剩余的负重对应结束的时间
            if (weekNumer == 0) {
                weekNumer = 1;
            }
//            insert(loadWeight, 1, startDate, DateFormatUtil.increaseOneDayOneSecondLess(weekNumer * 7-1, startDate), Global.TrainTime, weekNumer);
            subPlanEntityList.addAll(insert3Test(startDate, userId, newLoadWeight1, loadWeight, 2, weekNumer));
            String startDate1 = DateFormatUtil.increaseOneDayOneSecondLess(weekNumer * 7 - 1, startDate);
//            insert(newLoadWeight1, 2, startDate1, DateFormatUtil.increaseOneDayOneSecondLess(weekCount * 7-1, startDate1), Global.TrainTime, (12 + weekCount));
            subPlanEntityList.addAll(insert3Test(startDate1, userId, weight, newLoadWeight1, 3, weekCount));
        } else {
            String endDate = DateFormatUtil.increaseOneDayOneSecondLess(7 - 1, startDate);
//            insert(loadWeight, 1, startDate, endDate, Global.TrainTime, (12 + weekCount) * 7);
            subPlanEntityList.addAll(insert3Test(startDate, userId, newLoadWeight2, loadWeight, 1, 1));
            String classTwoStartDate = DateFormatUtil.getString2DateIncreaseOneDay(endDate, null);
//            insert(newLoadWeight2, 2, classTwoStartDate, DateFormatUtil.increaseOneDayOneSecondLess(11 * 7-1, classTwoStartDate), Global.TrainTime, (12 + weekCount));
            subPlanEntityList.addAll(insert3Test(classTwoStartDate, userId, newLoadWeight1, newLoadWeight2, 2, 11));
            String startDate1 = DateFormatUtil.increaseOneDayOneSecondLess(11 * 7 - 1, classTwoStartDate);
            String classThreeStartDate = DateFormatUtil.getString2DateIncreaseOneDay(startDate1, null);
//            insert(newLoadWeight1, 3, classThreeStartDate, DateFormatUtil.increaseOneDayOneSecondLess(weekCount * 7-1, classThreeStartDate), Global.TrainTime, (12 + weekCount));
            subPlanEntityList.addAll(insert3Test(classThreeStartDate, userId, weight, newLoadWeight1, 3, weekCount));
        }
        return subPlanEntityList;
    }

    public int calcInitLoad3(int targetLoad, String date, String weight,long planStartTime, long userId) {
        int lowerLimit = -100;
        int upLimit = 100;
        for (int m = 0; m < 100; m++) {
            List<TbSubPlan> subPlanEntityList = insertList3Test(date, lowerLimit, weight, userId);
            try {
                if (getLowerLimitValue(targetLoad, subPlanEntityList,planStartTime)) {
                    break;
                }
            } catch (Exception e) {
                if (e.toString().contains("null object reference")) {
                    return Integer.MAX_VALUE;
                }
            }
            lowerLimit = lowerLimit - 50;
        }
        if (lowerLimit <= -5100) {
            return Integer.MAX_VALUE;
        }
        for (int m = 0; m < 100; m++) {
            List<TbSubPlan> subPlanEntityList = insertList3Test(date, upLimit, weight, userId);
            if (getUpLimitValue(targetLoad, subPlanEntityList,planStartTime)) {
                break;
            }
            upLimit = upLimit + 50;
        }
        int lastLoad = 0;
        for (int i = lowerLimit; i < upLimit; i++) {
            List<TbSubPlan> subPlanEntityList = insertList3Test(date, i, weight, userId);
            TbSubPlan currentEntity = getThisDayLoadEntity(subPlanEntityList,planStartTime);
            if (lastLoad < currentEntity.getLoad() && currentEntity.getLoad() >= targetLoad) {
                return i;
            }
            lastLoad = currentEntity.getLoad();
        }
        return 0;
    }

    public int generatePlan3(int targetLoad, String date, String weightStr,int startStep,long planStartTime, long userId) {
        int weight = Integer.parseInt(weightStr);
        List<TbSubPlan> subPlanEntityList = new ArrayList<>();
        int newLoadWeight1 = (int) (weight * 0.87);
        int newLoadWeight2 = (int) (weight * 0.51);
        float weekDiff = (float) (weight * (0.87 - 0.51)) / 11;
        int weekCount = (int) Math.ceil((weight * (1 - 0.87) / weekDiff));//推算剩余的负重对应结束的时间
        if (weekCount == 0) {
            weekCount = 1;
        }
        String startDate = DateFormatUtil.getDate2String(planStartTime, "yyyy-MM-dd");
        startDate = startDate + " 00:00:00";
        if (targetLoad <= newLoadWeight2) {
            String endDate = DateFormatUtil.increaseOneDayOneSecondLess(7 - 1, startDate);
            TbPlan planEntity = insert(targetLoad, userId, weightStr, 1, startDate, endDate, Global.TrainTime, (12 + weekCount) * 7);
            insert(planEntity);
            subPlanEntityList.addAll(insert3Test(startDate, userId, newLoadWeight2, targetLoad, 1, 1));
            String classTwoStartDate = DateFormatUtil.getString2DateIncreaseOneDay(endDate, null);
            TbPlan planEntity1 = insert(newLoadWeight2, userId, weightStr, 2, classTwoStartDate, DateFormatUtil.increaseOneDayOneSecondLess(11 * 7 - 1, classTwoStartDate), Global.TrainTime, (12 + weekCount));
            insert(planEntity1);
            subPlanEntityList.addAll(insert3Test(classTwoStartDate, userId, newLoadWeight1, newLoadWeight2, 2, 11));
            String startDate1 = DateFormatUtil.increaseOneDayOneSecondLess(11 * 7 - 1, classTwoStartDate);
            String classThreeStartDate = DateFormatUtil.getString2DateIncreaseOneDay(startDate1, null);
            TbPlan planEntity2 = insert(newLoadWeight1, userId, weightStr, 3, classThreeStartDate, DateFormatUtil.increaseOneDayOneSecondLess(weekCount * 7 - 1, classThreeStartDate), Global.TrainTime, (12 + weekCount));
            insert(planEntity2);
            subPlanEntityList.addAll(insert3Test(classThreeStartDate, userId, weight, newLoadWeight1, 3, weekCount));
        } else {
            startDate = DateFormatUtil.getBeforeOrAfterDate(7, date);
            int weekNumer = (int) Math.ceil((weight * 0.87 - targetLoad) / weekDiff);//推算剩余的负重对应结束的时间
            if (weekNumer == 0) {
                weekNumer = 1;
            }
            int initValue = calcInitLoad3(targetLoad, date, weightStr,planStartTime, userId);
            TbPlan planEntity = insert(initValue, userId, weightStr, 1, startDate, DateFormatUtil.increaseOneDayOneSecondLess(weekNumer * 7 - 1, startDate), Global.TrainTime, weekNumer);
            insert(planEntity);
            subPlanEntityList.addAll(insert3Test(startDate, userId, newLoadWeight1, initValue, 2, weekNumer));
            String startDate1 = DateFormatUtil.increaseOneDayOneSecondLess(weekNumer * 7 - 1, startDate);
            TbPlan planEntity1 = insert(newLoadWeight1, userId, weightStr, 2, startDate1, DateFormatUtil.increaseOneDayOneSecondLess(weekCount * 7 - 1, startDate1), Global.TrainTime, (12 + weekCount));
            insert(planEntity1);
            subPlanEntityList.addAll(insert3Test(startDate1, userId, weight, newLoadWeight1, 3, weekCount));
        }
        List<TbSubPlan> modifySubPlanList = modifySubPlanData(subPlanEntityList, Global.TrainTime, startStep, Global.MaxTrainStep);
        List<TbSubPlan> subPlanEntities1 = modifyStartEndDate(planStartTime, modifySubPlanList);
        insertMany(subPlanEntities1);
        if (subPlanEntities1.size() <= 0) {
//            TrainPlanManager.getInstance().clearTrainPlanDatabaseByUserId(userId);
            return Integer.MAX_VALUE;
        } else {
            return 0;
        }

    }

    public List<TbSubPlan> insertList4Test(int loadWeight, String date, String weight, long userId) {//胫骨平台骨折（钢板固定）
        String startDate = date;
        List<TbSubPlan> subPlanEntityList = new ArrayList<>();
//        if (loadWeight >= 20)
//            loadWeight = 20;
        subPlanEntityList.addAll(insert2Test(startDate, userId, loadWeight, 1, 6));
        String endDate = DateFormatUtil.increaseOneDayOneSecondLess(6 * 7 - 1, startDate);
        String classTwoStartDate = DateFormatUtil.getString2DateIncreaseOneDay(endDate, null);
        subPlanEntityList.addAll(insert4Test(classTwoStartDate, userId, Integer.parseInt(weight), loadWeight, 2, 10));
        return subPlanEntityList;
    }

    public int calcInitLoad4(int targetLoad, String date, String weight,long planStartTime, long userId) {
        int lowerLimit = -100;
        int upLimit = 100;
        for (int m = 0; m < 100; m++) {
            List<TbSubPlan> subPlanEntityList = insertList4Test(lowerLimit, date, weight, userId);
            try {
                if (getLowerLimitValue(targetLoad, subPlanEntityList,planStartTime)) {
                    break;
                }
            } catch (Exception e) {
                if (e.toString().contains("null object reference")) {
                    return Integer.MAX_VALUE;
                }
            }
            lowerLimit = lowerLimit - 50;
        }
        if (lowerLimit <= -5100) {
            return Integer.MAX_VALUE;
        }
        for (int m = 0; m < 100; m++) {
            List<TbSubPlan> subPlanEntityList = insertList4Test(upLimit, date, weight, userId);
            if (getUpLimitValue(targetLoad, subPlanEntityList,planStartTime)) {
                break;
            }
            upLimit = upLimit + 50;
        }
        int lastLoad = 0;
        for (int i = lowerLimit; i < upLimit; i++) {
            List<TbSubPlan> subPlanEntityList = insertList4Test(i, date, weight, userId);
            TbSubPlan currentEntity = getThisDayLoadEntity(subPlanEntityList,planStartTime);
            if (lastLoad < currentEntity.getLoad() && currentEntity.getLoad() >= targetLoad) {
                return i;
            }
        }
        return 0;
    }

    public int generatePlan4(int targetLoad, String date, String weightStr,int startStep,long planStartTime, long userId) {
        int initValue = calcInitLoad4(targetLoad, date, weightStr,planStartTime, userId);
        if (initValue == Integer.MAX_VALUE)
            return initValue;
        List<TbSubPlan> subPlanEntityList = new ArrayList<>();
        String startDate = date;
        String endDate = DateFormatUtil.increaseOneDayOneSecondLess(6 * 7 - 1, startDate);
        String classTwoStartDate = null;
        if (targetLoad < 20) {
            subPlanEntityList.addAll(insert2Test(startDate, userId, initValue, 1, 6));
            TbPlan planEntity = insert(initValue, userId, weightStr, 1, startDate, endDate, Global.TrainTime, 6 * 7);
            if (planStartTime < DateFormatUtil.getString2Date(endDate)) {
                String date2String = DateFormatUtil.getDate2String(planStartTime, "yyyy-MM-dd");
                date2String = date2String + " 00:00:00";
                planEntity.setStartDateStr(date2String);
                insert(planEntity);
            } else {
                String date2String = DateFormatUtil.getDate2String(planStartTime, "yyyy-MM-dd");
                endDate = date2String + " 00:00:00";
            }
            classTwoStartDate = DateFormatUtil.getString2DateIncreaseOneDay(endDate, null);
        } else {
            classTwoStartDate = DateFormatUtil.getDate2String(planStartTime, "yyyy-MM-dd");
            classTwoStartDate = classTwoStartDate + " 00:00:00";
        }
        if (initValue < 0)
            initValue = targetLoad;
        TbPlan planEntity1 = insert(initValue, userId, weightStr, 2, classTwoStartDate, DateFormatUtil.increaseOneDayOneSecondLess(10 * 7 - 1, classTwoStartDate), Global.TrainTime, 10 * 7);
        insert(planEntity1);
        subPlanEntityList.addAll(insert4Test(classTwoStartDate, userId, Integer.parseInt(weightStr), initValue, 2, 10));
        List<TbSubPlan> modifySubPlanList = modifySubPlanData(subPlanEntityList, Global.TrainTime, startStep, Global.MaxTrainStep);
        insertMany(modifyStartEndDate(planStartTime, modifySubPlanList));
        return initValue;
    }

    public List<TbSubPlan> insertList5Test(int loadWeight, String date, String weight, long userId) {//胫骨平台骨折（钢板内固定）
        String startDate = date;
        List<TbSubPlan> subPlanEntityList = new ArrayList<>();
        subPlanEntityList.addAll(insert5Test(startDate, userId, Integer.parseInt(weight), loadWeight, 1, 40));
        return subPlanEntityList;
    }

    public List<TbSubPlan> insertList6Test(int loadWeight, String date, String weight, long userId) {//胫骨中段骨折（石膏固定）
        String startDate = date;
        List<TbSubPlan> subPlanEntityList = new ArrayList<>();
        subPlanEntityList.addAll(insert2Test(startDate, userId, loadWeight, 1, 3));
        String endDate = DateFormatUtil.increaseOneDayOneSecondLess(3 * 7 - 1, startDate);
        String classTwoStartDate = DateFormatUtil.getString2DateIncreaseOneDay(endDate, null);
        subPlanEntityList.addAll(insert5Test(classTwoStartDate, userId, Integer.parseInt(weight), loadWeight, 2, 24));
        return subPlanEntityList;
    }

    public List<TbSubPlan> insertList7Test(int loadWeight, String date, String weight, long userId) {//胫骨中段骨折（髓内钉）（桥接钢板）
        String startDate = date;
        return insert5Test(startDate, userId, Integer.parseInt(weight), loadWeight, 1, 24);
    }

    public List<TbSubPlan> insertList8Test(int loadWeight, String date, String weight, long userId) {//踝关节骨折（钢板内固定）
        String startDate = date;
        return insert4Test(startDate, userId, Integer.parseInt(weight), loadWeight, 1, 16);
    }

    public List<TbSubPlan> insertList9Test(int loadWeight, String date, String weightStr, long userId) {//跟骨骨折（钢板固定）
        String startDate = DateFormatUtil.getBeforeOrAfterDate(4 * 7, date);
        List<TbSubPlan> subPlanEntityList = new ArrayList<>();
//        if (loadWeight >= 10)
//            loadWeight = 10;
        subPlanEntityList.addAll(insert4Test(startDate, userId, 10, loadWeight, 1, 2));
        String endDate = DateFormatUtil.increaseOneDayOneSecondLess(2 * 7 - 1, startDate);
//        insert(loadWeight,1,startDate,endDate,Global.TrainTime,2*7);
        String classTwoStartDate = DateFormatUtil.getString2DateIncreaseOneDay(endDate, null);
//        insert(10,2,classTwoStartDate,DateFormatUtil.increaseOneDayOneSecondLess(2*7-1,classTwoStartDate),Global.TrainTime,2*7);
        subPlanEntityList.addAll(insert4Test(classTwoStartDate, userId, 10, 10, 2, 2));

        String startDate1 = DateFormatUtil.increaseOneDayOneSecondLess(2 * 7 - 1, classTwoStartDate);
        String classThreeStartDate = DateFormatUtil.getString2DateIncreaseOneDay(startDate1, null);
        String endDate1 = DateFormatUtil.increaseOneDayOneSecondLess(2 * 7 - 1, classThreeStartDate);
//        insert(20,3,classThreeStartDate,endDate1,Global.TrainTime,2*7);
        subPlanEntityList.addAll(insert4Test(classThreeStartDate, userId, 20, 20, 3, 2));
        String classFourStartDate = DateFormatUtil.getString2DateIncreaseOneDay(endDate1, null);
        int weight = Integer.parseInt(weightStr);
        int weekCount = 0;
        if (weight <= 40) {
            weekCount = 1;
//            insert(weight,4,classFourStartDate,DateFormatUtil.increaseOneDayOneSecondLess(weekCount*7-1,classFourStartDate),Global.TrainTime,weekCount*7);
            subPlanEntityList.addAll(insert4Test(classFourStartDate, userId, weight, weight, 4, weekCount));
        } else {
            weekCount = (weight - 40) / 10 + 1;
//            insert(40,4,classFourStartDate,DateFormatUtil.increaseOneDayOneSecondLess(weekCount*7-1,classFourStartDate),Global.TrainTime,weekCount*7);
            subPlanEntityList.addAll(insert4Test(classFourStartDate, userId, weight, 40, 4, weekCount));
        }
        return subPlanEntityList;
    }

    public List<TbSubPlan> insertList10Test(int loadWeight, String date, String weight, long userId) {//踝关节韧带损伤（踝关节韧带重建术）
        String startDate = date;
        List<TbSubPlan> subPlanEntityList = new ArrayList<>();
//        if (loadWeight >= 5)
//            loadWeight = 5;
        String endDate = DateFormatUtil.increaseOneDayOneSecondLess(4 * 7 - 1, startDate);
        subPlanEntityList.addAll(insert4Test(startDate, userId, loadWeight, loadWeight, 1, 4));
        String classTwoStartDate = DateFormatUtil.getString2DateIncreaseOneDay(endDate, null);
        subPlanEntityList.addAll(insert4Test(classTwoStartDate, userId, Integer.parseInt(weight), loadWeight, 2, 12));
        return subPlanEntityList;
    }

    public List<TbSubPlan> insertList11Test(int loadWeight, String date, String weightStr, long userId) {//股骨头坏死（腓骨移植术）
        String startDate = date;
        List<TbSubPlan> subPlanEntityList = new ArrayList<>();
//        if (loadWeight >= 12)
//            loadWeight = 12;
        String endDate = DateFormatUtil.increaseOneDayOneSecondLess(7 * 7 - 1, startDate);
//        insert(loadWeight,1,startDate,endDate,Global.TrainTime,7*7);
        subPlanEntityList.addAll(insert4Test(startDate, userId, 12, loadWeight, 1, 7));
        String classTwoStartDate = DateFormatUtil.getString2DateIncreaseOneDay(endDate, null);
        int weight = Integer.parseInt(weightStr);
        int diff = 5;
        int weekCount = ((weight - loadWeight) / diff) * 2;
//        insert(loadWeight,2,classTwoStartDate,DateFormatUtil.increaseOneDayOneSecondLess(weekCount*7-1,classTwoStartDate),Global.TrainTime,weekCount*7);
        subPlanEntityList.addAll(insert5Test(classTwoStartDate, userId, weight, loadWeight, 2, weekCount));
        return subPlanEntityList;
    }

    public int calcInitLoad5(int targetLoad, String date, String weight,long planStartTime, long userId) {
        int lowerLimit = -100;
        int upLimit = 100;
        for (int m = 0; m < 100; m++) {
            List<TbSubPlan> subPlanEntityList = insertList5Test(lowerLimit, date, weight, userId);
            try {
                if (getLowerLimitValue(targetLoad, subPlanEntityList,planStartTime)) {
                    break;
                }
            } catch (Exception e) {
                if (e.toString().contains("null object reference")) {
                    return Integer.MAX_VALUE;
                }
            }
            lowerLimit = lowerLimit - 50;
        }
        if (lowerLimit <= -5100) {
            return Integer.MAX_VALUE;
        }
        for (int m = 0; m < 100; m++) {
            List<TbSubPlan> subPlanEntityList = insertList5Test(upLimit, date, weight, userId);
            if (getUpLimitValue(targetLoad, subPlanEntityList,planStartTime)) {
                break;
            }
            upLimit = upLimit + 50;
        }
        int lastLoad = 0;
        for (int i = lowerLimit; i < upLimit; i++) {
            List<TbSubPlan> subPlanEntityList = insertList5Test(i, date, weight, userId);
            TbSubPlan currentEntity = getThisDayLoadEntity(subPlanEntityList,planStartTime);
            if (lastLoad < currentEntity.getLoad() && currentEntity.getLoad() >= targetLoad) {
                return i;
            }
        }
        return 0;
    }

    public int generatePlan5(int targetLoad, String date, String weightStr,int startStep,long planStartTime, long userId) {
        int initValue = calcInitLoad5(targetLoad, date, weightStr, planStartTime,userId);
        if (initValue == Integer.MAX_VALUE)
            return initValue;
        String startDate = date;
        TbPlan planEntity = insert(initValue, userId, weightStr, 1, startDate, DateFormatUtil.increaseOneDayOneSecondLess(39 * 7 - 1, startDate), Global.TrainTime, 39 * 7);
        String date2String = DateFormatUtil.getDate2String(planStartTime, "yyyy-MM-dd");
        date2String = date2String + " 00:00:00";
        planEntity.setStartDateStr(date2String);
        insert(planEntity);
        List<TbSubPlan> subPlanEntityList = insert5Test(startDate, userId, Integer.parseInt(weightStr), initValue, 1, 40);
        List<TbSubPlan> modifySubPlanList = modifySubPlanData(subPlanEntityList, Global.TrainTime, startStep, Global.MaxTrainStep);
        insertMany(modifyStartEndDate(planStartTime, modifySubPlanList));
        return initValue;
    }

    public int calcInitLoad6(int targetLoad, String date, String weight,long planStartTime, long userId) {
        int lowerLimit = -100;
        int upLimit = 100;
        for (int m = 0; m < 100; m++) {
            List<TbSubPlan> subPlanEntityList = insertList6Test(lowerLimit, date, weight, userId);
            try {
                if (getLowerLimitValue(targetLoad, subPlanEntityList,planStartTime)) {
                    break;
                }
            } catch (Exception e) {
                if (e.toString().contains("null object reference")) {
                    return Integer.MAX_VALUE;
                }
            }
            lowerLimit = lowerLimit - 50;
        }
        if (lowerLimit <= -5100) {
            return Integer.MAX_VALUE;
        }
        for (int m = 0; m < 100; m++) {
            List<TbSubPlan> subPlanEntityList = insertList6Test(upLimit, date, weight, userId);
            if (getUpLimitValue(targetLoad, subPlanEntityList,planStartTime)) {
                break;
            }
            upLimit = upLimit + 50;
        }
        int lastLoad = 0;
        for (int i = lowerLimit; i < upLimit; i++) {
            List<TbSubPlan> subPlanEntityList = insertList6Test(i, date, weight, userId);
            TbSubPlan currentEntity = getThisDayLoadEntity(subPlanEntityList,planStartTime);
            if (lastLoad < currentEntity.getLoad() && currentEntity.getLoad() >= targetLoad) {
                return i;
            }
        }
        return 0;
    }

    public int calcInitLoad7(int targetLoad, String date, String weight,long planStartTime, long userId) {
        int lowerLimit = -100;
        int upLimit = 100;
        for (int m = 0; m < 100; m++) {
            List<TbSubPlan> subPlanEntityList = insertList7Test(lowerLimit, date, weight, userId);
            try {
                if (getLowerLimitValue(targetLoad, subPlanEntityList,planStartTime)) {
                    break;
                }
            } catch (Exception e) {
                if (e.toString().contains("null object reference")) {
                    return Integer.MAX_VALUE;
                }
            }
            lowerLimit = lowerLimit - 50;
        }
        if (lowerLimit <= -5100) {
            return Integer.MAX_VALUE;
        }
        for (int m = 0; m < 100; m++) {
            List<TbSubPlan> subPlanEntityList = insertList7Test(upLimit, date, weight, userId);
            if (getUpLimitValue(targetLoad, subPlanEntityList,planStartTime)) {
                break;
            }
            upLimit = upLimit + 50;
        }
        int lastLoad = 0;
        for (int i = lowerLimit; i < upLimit; i++) {
            List<TbSubPlan> subPlanEntityList = insertList7Test(i, date, weight, userId);
            TbSubPlan currentEntity = getThisDayLoadEntity(subPlanEntityList,planStartTime);
            if (lastLoad < currentEntity.getLoad() && currentEntity.getLoad() >= targetLoad) {
                return i;
            }
        }
        return 0;
    }

    public int calcInitLoad8(int targetLoad, String date, String weight,long planStartTime, long userId) {
        int lowerLimit = -100;
        int upLimit = 100;
        for (int m = 0; m < 100; m++) {
            List<TbSubPlan> subPlanEntityList = insertList8Test(lowerLimit, date, weight, userId);
            try {
                if (getLowerLimitValue(targetLoad, subPlanEntityList,planStartTime)) {
                    break;
                }
            } catch (Exception e) {
                if (e.toString().contains("null object reference")) {
                    return Integer.MAX_VALUE;
                }
            }
            lowerLimit = lowerLimit - 50;
        }
        if (lowerLimit <= -5100) {
            return Integer.MAX_VALUE;
        }
        for (int m = 0; m < 100; m++) {
            List<TbSubPlan> subPlanEntityList = insertList8Test(upLimit, date, weight, userId);
            if (getUpLimitValue(targetLoad, subPlanEntityList,planStartTime)) {
                break;
            }
            upLimit = upLimit + 50;
        }
        int lastLoad = 0;
        for (int i = lowerLimit; i < upLimit; i++) {
            List<TbSubPlan> subPlanEntityList = insertList8Test(i, date, weight, userId);
            TbSubPlan currentEntity = getThisDayLoadEntity(subPlanEntityList,planStartTime);
            if (lastLoad < currentEntity.getLoad() && currentEntity.getLoad() >= targetLoad) {
                return i;
            }
        }
        return 0;
    }

    public int calcInitLoad10(int targetLoad, String date, String weight,long planStartTime, long userId) {
        int lowerLimit = -100;
        int upLimit = 100;
        for (int m = 0; m < 100; m++) {
            List<TbSubPlan> subPlanEntityList = insertList10Test(lowerLimit, date, weight, userId);
            try {
                if (getLowerLimitValue(targetLoad, subPlanEntityList,planStartTime)) {
                    break;
                }
            } catch (Exception e) {
                if (e.toString().contains("null object reference")) {
                    return Integer.MAX_VALUE;
                }
            }
            lowerLimit = lowerLimit - 50;
        }
        if (lowerLimit <= -5100) {
            return Integer.MAX_VALUE;
        }
        for (int m = 0; m < 100; m++) {
            List<TbSubPlan> subPlanEntityList = insertList10Test(upLimit, date, weight, userId);
            if (getUpLimitValue(targetLoad, subPlanEntityList, planStartTime)) {
                break;
            }
            upLimit = upLimit + 50;
        }
        int lastLoad = 0;
        for (int i = lowerLimit; i < upLimit; i++) {
            List<TbSubPlan> subPlanEntityList = insertList10Test(i, date, weight, userId);
            TbSubPlan currentEntity = getThisDayLoadEntity(subPlanEntityList,planStartTime);
            if (lastLoad < currentEntity.getLoad() && currentEntity.getLoad() >= targetLoad) {
                return i;
            }
        }
        return 0;
    }

    public int calcInitLoad11(int targetLoad, String date, String weight,long planStartTime, long userId) {
        int lowerLimit = -100;
        int upLimit = 100;
        for (int m = 0; m < 100; m++) {
            List<TbSubPlan> subPlanEntityList = insertList11Test(lowerLimit, date, weight, userId);
            try {
                if (getLowerLimitValue(targetLoad, subPlanEntityList,planStartTime)) {
                    break;
                }
            } catch (Exception e) {
                if (e.toString().contains("null object reference")) {
                    return Integer.MAX_VALUE;
                }
            }
            lowerLimit = lowerLimit - 50;
        }
        if (lowerLimit <= -5100) {
            return Integer.MAX_VALUE;
        }
        for (int m = 0; m < 100; m++) {
            List<TbSubPlan> subPlanEntityList = insertList11Test(upLimit, date, weight, userId);
            if (getUpLimitValue(targetLoad, subPlanEntityList,planStartTime)) {
                break;
            }
            upLimit = upLimit + 50;
        }
        int lastLoad = 0;
        for (int i = lowerLimit; i < upLimit; i++) {
            List<TbSubPlan> subPlanEntityList = insertList11Test(i, date, weight, userId);
            TbSubPlan currentEntity = getThisDayLoadEntity(subPlanEntityList,planStartTime);
            if (lastLoad < currentEntity.getLoad() && currentEntity.getLoad() >= targetLoad) {
                return i;
            }
        }
        return 0;
    }

    public int generatePlan6(int targetLoad, String date, String weightStr,int startStep,long planStartTime, long userId) {
        int initValue = calcInitLoad6(targetLoad, date, weightStr,planStartTime, userId);
        if (initValue == Integer.MAX_VALUE)
            return initValue;
        List<TbSubPlan> subPlanEntityList = new ArrayList<>();
        String startDate = date;
        subPlanEntityList.addAll(insert2Test(startDate, userId, initValue, 1, 3));
        String endDate = DateFormatUtil.increaseOneDayOneSecondLess(3 * 7 - 1, startDate);
        TbPlan planEntity = insert(initValue, userId, weightStr, 1, startDate, endDate, Global.TrainTime, 3 * 7);
        if (planStartTime < DateFormatUtil.getString2Date(endDate)) {
            String date2String = DateFormatUtil.getDate2String(planStartTime, "yyyy-MM-dd");
            date2String = date2String + " 00:00:00";
            planEntity.setStartDateStr(date2String);
            insert(planEntity);
        } else {
//            String date2String = DateFormatUtil.getDate2String(System.currentTimeMillis(),"yyyy-MM-dd");
//            endDate = date2String + " 00:00:00";
//            initValue = targetLoad;
        }
        String classTwoStartDate = DateFormatUtil.getString2DateIncreaseOneDay(endDate, null);
        subPlanEntityList.addAll(insert5Test(classTwoStartDate, userId, Integer.parseInt(weightStr), initValue, 2, 24));
        TbPlan planEntity1 = insert(initValue, userId, weightStr, 2, classTwoStartDate, DateFormatUtil.increaseOneDayOneSecondLess(24 * 7 - 1, classTwoStartDate), Global.TrainTime, 24 * 7);
        insert(planEntity1);

        List<TbSubPlan> modifySubPlanList = modifySubPlanData(subPlanEntityList, Global.TrainTime, startStep, Global.MaxTrainStep);
        insertMany(modifyStartEndDate(planStartTime, modifySubPlanList));
        return initValue;
    }

    public int generatePlan7(int targetLoad, String date, String weightStr,int startStep,long planStartTime, long userId) {
        int initValue = calcInitLoad7(targetLoad, date, weightStr, planStartTime,userId);
        if (initValue == Integer.MAX_VALUE)
            return initValue;
        String startDate = date;
        TbPlan planEntity = insert(initValue, userId, weightStr, 1, startDate, DateFormatUtil.increaseOneDayOneSecondLess(24 * 7 - 1, startDate), Global.TrainTime, 24 * 7);
        List<TbSubPlan> subPlanEntityList = insert5Test(startDate, userId, Integer.parseInt(weightStr), initValue, 1, 24);
        insert(planEntity);
        List<TbSubPlan> modifySubPlanList = modifySubPlanData(subPlanEntityList, Global.TrainTime, startStep, Global.MaxTrainStep);
        insertMany(modifyStartEndDate(planStartTime, modifySubPlanList));
        return initValue;
    }

    public int generatePlan8(int targetLoad, String date, String weightStr,int startStep,long planStartTime, long userId) {
        int initValue = calcInitLoad8(targetLoad, date, weightStr,planStartTime, userId);
        if (initValue == Integer.MAX_VALUE)
            return initValue;
        String startDate = date;
        TbPlan planEntity = insert(initValue, userId, weightStr, 1, startDate, DateFormatUtil.increaseOneDayOneSecondLess(16 * 7 - 1, startDate), Global.TrainTime, 16 * 7);
        List<TbSubPlan> subPlanEntityList = insert4Test(startDate, userId, Integer.parseInt(weightStr), initValue, 1, 16);
        insert(planEntity);
        List<TbSubPlan> modifySubPlanList = modifySubPlanData(subPlanEntityList, Global.TrainTime, startStep, Global.MaxTrainStep);
        insertMany(modifyStartEndDate(planStartTime, modifySubPlanList));
        return initValue;
    }

    public int generatePlan9(int targetLoad, String date, String weightStr,int startStep,long planStartTime, long userId) {
        List<TbSubPlan> subPlanEntityList = new ArrayList<>();
        String startZero = date;
        String startDate = DateFormatUtil.getBeforeOrAfterDate(4 * 7, date);
        List<TbSubPlan> subPlanEntities = insert2Test(startZero, userId, 0, 1, 4);
        List<TbSubPlan> modifySubPlanEntities = modifySubPlanData(subPlanEntities, Global.TrainTime, 5, 20);
        insertMany(modifyStartEndDate(planStartTime, modifySubPlanEntities));
        String endDate = DateFormatUtil.increaseOneDayOneSecondLess(2 * 7 - 1, startDate);
        if (targetLoad < 10) {
            subPlanEntityList.addAll(insert4Test(startDate, userId, 10, targetLoad, 1, 2));
            TbPlan planEntity = insert(10, userId, weightStr, 1, startDate, endDate, Global.TrainTime, 2 * 7);
            insert(planEntity);
            String classTwoStartDate = DateFormatUtil.getString2DateIncreaseOneDay(endDate, null);
            TbPlan planEntity1 = insert(10, userId, weightStr, 2, classTwoStartDate, DateFormatUtil.increaseOneDayOneSecondLess(2 * 7 - 1, classTwoStartDate), Global.TrainTime, 2 * 7);
            insert(planEntity1);
            subPlanEntityList.addAll(insert4Test(classTwoStartDate, userId, 10, 10, 2, 2));
        } else {
//            String classTwoStartDate = DateFormatUtil.getString2DateIncreaseOneDay(endDate,null);
            TbPlan planEntity1 = insert(10, userId, weightStr, 2, endDate, DateFormatUtil.increaseOneDayOneSecondLess(4 * 7 - 1, endDate), Global.TrainTime, 4 * 7);
            insert(planEntity1);
            subPlanEntityList.addAll(insert4Test(endDate, userId, 10, 10, 2, 4));
        }


        String startDate1 = DateFormatUtil.increaseOneDayOneSecondLess(4 * 7 - 1, DateFormatUtil.getString2DateIncreaseOneDay(endDate, null));
        String classThreeStartDate = DateFormatUtil.getString2DateIncreaseOneDay(startDate1, null);
        String endDate1 = DateFormatUtil.increaseOneDayOneSecondLess(2 * 7 - 1, classThreeStartDate);
        TbPlan planEntity2 = insert(20, userId, weightStr, 3, classThreeStartDate, endDate1, Global.TrainTime, 2 * 7);
        insert(planEntity2);
        subPlanEntityList.addAll(insert4Test(classThreeStartDate, userId, 20, 20, 3, 2));
        String classFourStartDate = DateFormatUtil.getString2DateIncreaseOneDay(endDate1, null);
        int weight = Integer.parseInt(weightStr);
        int weekCount = 0;
        TbPlan planEntity3;
        if (weight <= 40) {
            weekCount = 1;
            planEntity3 = insert(weight, userId, weightStr, 4, classFourStartDate, DateFormatUtil.increaseOneDayOneSecondLess(weekCount * 7 - 1, classFourStartDate), Global.TrainTime, weekCount * 7);
            subPlanEntityList.addAll(insert4Test(classFourStartDate, userId, weight, weight, 4, weekCount));
        } else {
            weekCount = (weight - 40) / 10 + 1;
            planEntity3 = insert(40, userId, weightStr, 4, classFourStartDate, DateFormatUtil.increaseOneDayOneSecondLess(weekCount * 7 - 1, classFourStartDate), Global.TrainTime, weekCount * 7);
            subPlanEntityList.addAll(insert4Test(classFourStartDate, userId, weight, 40, 4, weekCount));
        }
        insert(planEntity3);
        List<TbSubPlan> modifySubPlanList = modifySubPlanData(subPlanEntityList, Global.TrainTime, startStep, Global.MaxTrainStep);
        List<TbSubPlan> subPlanEntities1 = modifyStartEndDate(planStartTime, modifySubPlanList);
        insertManyTest(subPlanEntities1);
        if (subPlanEntities1.size() <= 0) {
//            TrainPlanManager.getInstance().clearTrainPlanDatabaseByUserId(userId);
            return Integer.MAX_VALUE;
        } else {
            return 0;
        }
    }

    public int generatePlan10(int targetLoad, String date, String weightStr,int startStep,long planStartTime, long userId) {
        int initValue = calcInitLoad10(targetLoad, date, weightStr,planStartTime, userId);
        if (initValue == Integer.MAX_VALUE)
            return initValue;
        List<TbSubPlan> subPlanEntityList = new ArrayList<>();
        String startDate = date;
        String endDate = DateFormatUtil.increaseOneDayOneSecondLess(4 * 7 - 1, startDate);
        String classTwoStartDate = DateFormatUtil.getString2DateIncreaseOneDay(endDate, null);
        if (initValue < 5) {
            initValue = 5;
            subPlanEntityList.addAll(insert4Test(startDate, userId, initValue, initValue, 1, 4));
            TbPlan planEntity = insert(initValue, userId, weightStr, 1, startDate, endDate, Global.TrainTime, 4 * 7);
            if (planStartTime < DateFormatUtil.getString2Date(endDate)) {
                String date2String = DateFormatUtil.getDate2String(planStartTime, "yyyy-MM-dd");
                date2String = date2String + " 00:00:00";
                planEntity.setStartDateStr(date2String);
                insert(planEntity);
            } else {
//                String date2String = DateFormatUtil.getDate2String(System.currentTimeMillis(),"yyyy-MM-dd");
//                endDate = date2String + " 00:00:00";
//                classTwoStartDate = DateFormatUtil.getString2DateIncreaseOneDay(endDate,null);
            }

        } else {
            classTwoStartDate = DateFormatUtil.getDate2String(planStartTime, "yyyy-MM-dd");
            classTwoStartDate = classTwoStartDate + " 00:00:00";
        }
        TbPlan planEntity1 = insert(initValue, userId, weightStr, 2, classTwoStartDate, DateFormatUtil.increaseOneDayOneSecondLess(12 * 7 - 1, classTwoStartDate), Global.TrainTime, 12 * 7);
        insert(planEntity1);
        subPlanEntityList.addAll(insert4Test(classTwoStartDate, userId, Integer.parseInt(weightStr), initValue, 2, 12));
        List<TbSubPlan> modifySubPlanList = modifySubPlanData(subPlanEntityList, Global.TrainTime, startStep, Global.MaxTrainStep);
        insertMany(modifyStartEndDate(planStartTime, modifySubPlanList));
        return initValue;
    }

    public int generatePlan11(int targetLoad, String date, String weightStr,int startStep,long planStartTime, long userId) {
        int initValue = calcInitLoad11(targetLoad, date, weightStr,planStartTime, userId);
        if (initValue == Integer.MAX_VALUE)
            return initValue;
        List<TbSubPlan> subPlanEntityList = new ArrayList<>();
        String startDate = date;
        String endDate = DateFormatUtil.increaseOneDayOneSecondLess(7 * 7 - 1, startDate);
        String classTwoStartDate = null;
        if (targetLoad < 12) {
            subPlanEntityList.addAll(insert4Test(startDate, userId, 12, initValue, 1, 7));
            TbPlan planEntity = insert(targetLoad, userId, weightStr, 1, startDate, endDate, Global.TrainTime, 7 * 7);
            if (planStartTime < DateFormatUtil.getString2Date(endDate)) {
                String date2String = DateFormatUtil.getDate2String(planStartTime, "yyyy-MM-dd");
                date2String = date2String + " 00:00:00";
                planEntity.setStartDateStr(date2String);
                insert(planEntity);
            } else {
                String date2String = DateFormatUtil.getDate2String(planStartTime, "yyyy-MM-dd");
                endDate = date2String + " 00:00:00";
            }
            classTwoStartDate = DateFormatUtil.getString2DateIncreaseOneDay(endDate, null);
            initValue = 12;
        } else {
            classTwoStartDate = DateFormatUtil.getDate2String(planStartTime, "yyyy-MM-dd");
            classTwoStartDate = classTwoStartDate + " 00:00:00";
        }
        int weight = Integer.parseInt(weightStr);
        int diff = 5;
        int weekCount = ((weight - 12) / diff) * 2;
        TbPlan planEntity1 = insert(12, userId, weightStr, 2, classTwoStartDate, DateFormatUtil.increaseOneDayOneSecondLess(weekCount * 7 - 1, classTwoStartDate), Global.TrainTime, weekCount * 7);
        insert(planEntity1);
        subPlanEntityList.addAll(insert5Test(classTwoStartDate, userId, weight, initValue, 2, weekCount));
        List<TbSubPlan> modifySubPlanList = modifySubPlanData(subPlanEntityList, Global.TrainTime, startStep, Global.MaxTrainStep);
        insertMany(modifyStartEndDate(planStartTime, modifySubPlanList));
        return initValue;
    }

    public int generatePlan2(int targetLoad, String date, String weight,int startStep,long planStartTime, long userId) {
        int initValue = calcInitLoad(targetLoad, date, weight,planStartTime, userId);
        if (initValue == Integer.MAX_VALUE)
            return initValue;
        String startDate = date;
        String endDate = DateFormatUtil.increaseOneDayOneSecondLess(6 * 7 - 1, startDate);
        TbPlan planEntity = insert(initValue, userId, weight, 1, startDate, endDate, Global.TrainTime, 6 * 7);
        planEntity.setStartDateStr(DateFormatUtil.getNowDate());
        insert(planEntity);

        List<TbSubPlan> subPlanEntityList = insertTest(startDate, weight, userId, initValue, 1, 6);
        List<TbSubPlan> modifySubPlanList = modifySubPlanData(subPlanEntityList, Global.TrainTime, startStep, Global.MaxTrainStep);
        insertMany(modifyStartEndDate(planStartTime, modifySubPlanList));
        return initValue;
    }

    public void insert(TbPlan entity) {//插入到tb_plan表中
//        planEntityDao.insertOrReplace(entity);
        try {
            entity.setStartDate(sdf.parse(entity.getStartDateStr()));
            entity.setEndDate(sdf.parse(entity.getEndDateStr()));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        planService.save(entity);
    }

    public int generatePlan1(int targetLoad, String date, String weight, int startStep,long planStartTime,long userId) {
        int initValue = calcInitLoad(targetLoad, date, weight,planStartTime, userId);
        if (initValue == Integer.MAX_VALUE)
            return initValue;
        String startDate = date;
        String endDate = DateFormatUtil.increaseOneDayOneSecondLess(6 * 7 - 1, startDate);
        TbPlan planEntity = insert(initValue, userId, weight, 1, startDate, endDate, 8, 6 * 7);
        if (planStartTime < DateFormatUtil.getString2Date(endDate)) {
            planEntity.setStartDate(new Date());
            insert(planEntity);
        } else {
            String date2String = DateFormatUtil.getDate2String(planStartTime, "yyyy-MM-dd");
            endDate = date2String + " 00:00:00";
        }
        String classTwoStartDate = DateFormatUtil.getString2DateIncreaseOneDay(endDate, null);
        int newLoadWeight = (int) (((Float.parseFloat(weight) - initValue) / 6) * 4 + initValue);//体重-评估负重 6周平均分配
        TbPlan planEntity1 = insert(newLoadWeight, userId, weight, 2, classTwoStartDate, DateFormatUtil.increaseOneDayOneSecondLess(2 * 7 - 1, classTwoStartDate), 25, 6 * 7);
        insert(planEntity1);

        List<TbSubPlan> subPlanEntityList = insertTest(startDate, weight, userId, initValue, 1, 6);
        List<TbSubPlan> modifySubPlanList = modifySubPlanData(subPlanEntityList, 25, startStep, Global.MaxTrainStep);
        insertMany(modifyStartEndDate(planStartTime, modifySubPlanList));
        return initValue;
    }

    private List<TbSubPlan> modifyStartEndDate(long startTime, List<TbSubPlan> list) {
        List<TbSubPlan> listResult = new ArrayList<>();
        int i = 0;
        String startDate = DateFormatUtil.getDate2String(startTime, "yyyy-MM-dd");
        startDate = startDate + " 00:00:00";
        for (TbSubPlan subPlanEntity : list) {
            if (startTime <= DateFormatUtil.getString2Date(subPlanEntity.getEndDateStr())) {
                if (subPlanEntity.getLoad() <= 0)
                    subPlanEntity.setLoad(2);
                listResult.add(subPlanEntity);
            }
        }
        return listResult;
    }

    public List<TbSubPlan> modifySubPlanData(List<TbSubPlan> subPlanEntityList, int defaultTrainTime, int minTrainStep, int maxTrainStep) {//子计划增加训练步数和训练时间
        List<TbSubPlan> planEntityList = new ArrayList<>();
        if (subPlanEntityList.size() <= 1)
            return subPlanEntityList;
        if (minTrainStep >= maxTrainStep) {
            minTrainStep = maxTrainStep;
        }
        int diffPerStep = (maxTrainStep - minTrainStep) / (subPlanEntityList.size() - 1);
        for (int i = 0; i < subPlanEntityList.size(); i++) {
            TbSubPlan subPlanEntity = subPlanEntityList.get(i);
            int trainStep;
            if (i == subPlanEntityList.size() - 1) {
                trainStep = maxTrainStep;
            } else {
                trainStep = minTrainStep + diffPerStep * i;
            }
            subPlanEntity.setTrainStep(trainStep);
            int trainTime = (int) Math.ceil(trainStep * 1.0 / Global.TrainCountMinute);
            if (trainTime < defaultTrainTime) {
                trainTime = defaultTrainTime;
            }
//            if (subPlanEntity.getLoad() < 0){
//                subPlanEntity.setLoad(0);
//            }
            subPlanEntity.setTrainTime(trainTime);
            subPlanEntity.setModifyStatus(0);
            planEntityList.add(subPlanEntity);
        }
        return planEntityList;
    }

    public void insertManyTest(List<TbSubPlan> subPlanEntityList) {//插入到tb_sub_plan表中
        if (subPlanEntityList != null && subPlanEntityList.size() > 0) {
//            subPlanEntityDao.insertOrReplaceInTx(subPlanEntityList);
            for (TbSubPlan tbSubPlan : subPlanEntityList) {
                tbSubPlan.setCreateDate(new Date());
                try {
                    tbSubPlan.setStartDate(sdf.parse(tbSubPlan.getStartDateStr()));
                    tbSubPlan.setEndDate(sdf.parse(tbSubPlan.getEndDateStr()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }


            }
            subPlanService.saveBatch(subPlanEntityList);
        }
    }

    public void insertMany(List<TbSubPlan> subPlanEntityList) {//插入到tb_sub_plan表中
        if (subPlanEntityList != null && subPlanEntityList.size() > 0) {
//            clearPlanByUserId(subPlanEntityList.get(0).getUserId());
//            subPlanEntityDao.insertOrReplaceInTx(subPlanEntityList);
            for (TbSubPlan tbSubPlan : subPlanEntityList) {
                tbSubPlan.setCreateDate(new Date());
                try {
                    tbSubPlan.setStartDate(sdf.parse(tbSubPlan.getStartDateStr()));
                    tbSubPlan.setEndDate(sdf.parse(tbSubPlan.getEndDateStr()));
                } catch (ParseException e) {
                    e.printStackTrace();
                }


            }
            subPlanService.saveBatch(subPlanEntityList);
        }
    }

    public List<TbSubPlan> insert2Test(String startDate, long userId, int loadWeight, int classId, int weekTotal) {
        List<TbSubPlan> subPlanEntityList = new ArrayList<>();
        for (int i = 0; i < weekTotal; i++) {
            TbSubPlan subPlanEntity = new TbSubPlan();
            subPlanEntity.setKeyId(SnowflakeIdUtil.getUniqueId());
            subPlanEntity.setClassId(classId);
            subPlanEntity.setUserId(userId);
            subPlanEntity.setLoad(loadWeight);
            subPlanEntity.setWeekNum(i + 1);
            subPlanEntity.setDayNum(0);
            subPlanEntity.setPlanStatus(0);
            subPlanEntity.setStartDateStr(DateFormatUtil.getBeforeOrAfterDate(i * 7, startDate));
            if (i == weekTotal - 1) {
                subPlanEntity.setEndDateStr(DateFormatUtil.increaseOneDayOneSecondLess((i + 1) * 7 - 1, startDate));
            } else {
                subPlanEntity.setEndDateStr(DateFormatUtil.getBeforeOrAfterDate((i + 1) * 7, startDate));
            }


            subPlanEntityList.add(subPlanEntity);
        }
        return subPlanEntityList;
    }

    public List<TbSubPlan> insert3Test(String startDate, long userId, int desWeight, int loadWeight, int classId, int weekTotal) {
        List<TbSubPlan> subPlanEntityList = new ArrayList<>();
        for (int i = 0; i < weekTotal; i++) {
            TbSubPlan subPlanEntity = new TbSubPlan();
            subPlanEntity.setKeyId(SnowflakeIdUtil.getUniqueId());
            subPlanEntity.setClassId(classId);
            subPlanEntity.setUserId(userId);
            if (weekTotal <= 1) {
                subPlanEntity.setLoad(loadWeight);
            } else {
                subPlanEntity.setLoad(loadWeight + (desWeight - loadWeight) * i / (weekTotal - 1));
            }
            subPlanEntity.setPlanStatus(0);
            if (classId == 1) {
                subPlanEntity.setStartDateStr(DateFormatUtil.getBeforeOrAfterDate(i, startDate));
                if (i == weekTotal - 1) {
                    subPlanEntity.setEndDateStr(DateFormatUtil.increaseOneDayOneSecondLess((i + 1) * 7 - 1, startDate));
                } else {
                    subPlanEntity.setEndDateStr(DateFormatUtil.getBeforeOrAfterDate((i + 1) * 7, startDate));
                }


                subPlanEntity.setWeekNum(1);
//                subPlanEntity.setDayNum(i+1);
            } else {
                subPlanEntity.setStartDateStr(DateFormatUtil.getBeforeOrAfterDate(i * 7, startDate));
                if (i == weekTotal - 1) {
                    subPlanEntity.setEndDateStr(DateFormatUtil.increaseOneDayOneSecondLess((i + 1) * 7 - 1, startDate));
                } else {
                    subPlanEntity.setEndDateStr(DateFormatUtil.getBeforeOrAfterDate((i + 1) * 7, startDate));
                }


                subPlanEntity.setWeekNum(i + 1);
                subPlanEntity.setDayNum(0);
            }
            subPlanEntityList.add(subPlanEntity);
//            subPlanEntityDao.insertOrReplace(subPlanEntity);
        }
        return subPlanEntityList;
    }

    public List<TbSubPlan> insert4Test(String startDate, long userId, int desWeight, int loadWeight, int classId, int weekTotal) {
        List<TbSubPlan> subPlanEntityList = new ArrayList<>();
        for (int i = 0; i < weekTotal; i++) {
            TbSubPlan subPlanEntity = new TbSubPlan();
            subPlanEntity.setKeyId(SnowflakeIdUtil.getUniqueId());
            subPlanEntity.setClassId(classId);
            subPlanEntity.setUserId(userId);
            if (weekTotal == 1) {
                subPlanEntity.setLoad(loadWeight + (desWeight - loadWeight) * i);
            } else {
                subPlanEntity.setLoad(loadWeight + (desWeight - loadWeight) * i / (weekTotal - 1));
            }
            subPlanEntity.setWeekNum(i + 1);
            subPlanEntity.setDayNum(0);
            subPlanEntity.setPlanStatus(0);
            subPlanEntity.setStartDateStr(DateFormatUtil.getBeforeOrAfterDate(i * 7, startDate));
            if (i == weekTotal - 1) {
                subPlanEntity.setEndDateStr(DateFormatUtil.increaseOneDayOneSecondLess((i + 1) * 7 - 1, startDate));
            } else {
                subPlanEntity.setEndDateStr(DateFormatUtil.getBeforeOrAfterDate((i + 1) * 7, startDate));
            }


            subPlanEntity.setKeyId(SnowflakeIdUtil.getUniqueId());
            subPlanEntityList.add(subPlanEntity);
        }
        return subPlanEntityList;
    }

    public List<TbSubPlan> insert5Test(String startDate, long userId, int desWeight, int loadWeight, int classId, int weekTotal) {
        List<TbSubPlan> subPlanEntityList = new ArrayList<>();
//        if (weekTotal/2 == 1)
//            weekTotal = weekTotal + 1;
        for (int i = 0; i < weekTotal; i++) {
            TbSubPlan subPlanEntity = new TbSubPlan();
            subPlanEntity.setKeyId(SnowflakeIdUtil.getUniqueId());
            subPlanEntity.setClassId(classId);
            subPlanEntity.setUserId(userId);
            if (weekTotal == 1) {
                subPlanEntity.setLoad(loadWeight + (desWeight - loadWeight) * i);
            } else {
                subPlanEntity.setLoad(loadWeight + (desWeight - loadWeight) * i / (weekTotal - 1));
            }
            subPlanEntity.setWeekNum(i + 1);
            subPlanEntity.setDayNum(0);
            subPlanEntity.setPlanStatus(0);
            subPlanEntity.setStartDateStr(DateFormatUtil.getBeforeOrAfterDate(i * 7, startDate));
            if (i == weekTotal - 1) {
                subPlanEntity.setEndDateStr(DateFormatUtil.increaseOneDayOneSecondLess((i + 1) * 7 - 1, startDate));
            } else {
                subPlanEntity.setEndDateStr(DateFormatUtil.getBeforeOrAfterDate((i + 1) * 7, startDate));
            }


            subPlanEntityList.add(subPlanEntity);
        }
        return subPlanEntityList;
    }

    public TbPlan insert(int loadWeight, long userId, String weight, int classId, String startDate, String endDate, int trainTime, int planTotalDay) {
//        PlanEntity planEntity = planEntityDao.queryBuilder().where(PlanEntityDao.Properties.UserId.eq(SPHelper.getUserId()),PlanEntityDao.Properties.ClassId.eq(classId)).unique();
//        if (planEntity == null){
//            planEntity = new PlanEntity();
//            planEntity.setCreateDate(DateFormatUtil.getNowDate());
//        }
        TbPlan planEntity = new TbPlan();
        planEntity.setCreateDate(new Date());

        planEntity.setStartDateStr(startDate);
        planEntity.setEndDateStr(endDate);

        planEntity.setWeight(weight);
        planEntity.setUserId(userId);
        planEntity.setUpdateDate(new Date());
        planEntity.setClassId(classId);
        planEntity.setLoad(loadWeight);

        planEntity.setTimeOfDay(3);//每天训练次数
        planEntity.setCountOfTime(trainTime * trainCountMinute);//每次训练几步
        planEntity.setPlanStatus(0);//计划状态 0未开始，1进行中，2完成
        planEntity.setPlanId(SnowflakeIdUtil.getUniqueId());
        planEntity.setTrainType(1);//训练方式 0按步数，1按时间
        planEntity.setPlanTotalDay(planTotalDay);//总训练周期（天）
        planEntity.setTrainTime(trainTime);//训练时间(分钟)
        planEntity.setKeyId(SnowflakeIdUtil.getUniqueId());
//        planEntityDao.insertOrReplace(planEntity);
        return planEntity;
    }

    public boolean getLowerLimitValue(int targetLoad, List<TbSubPlan> subPlanEntityList,long planStartTime) {
        TbSubPlan currentEntity = getThisDayLoadEntity(subPlanEntityList,planStartTime);
        return currentEntity.getLoad() <= targetLoad;
    }

    public TbSubPlan getThisDayLoadEntity(List<TbSubPlan> subPlanEntityList,long planStartTime) {
        TbSubPlan currentEntity = null;
        TbSubPlan nextEntity = null;
        for (int i = 0; i < subPlanEntityList.size(); i++) {
            TbSubPlan subPlanEntity = subPlanEntityList.get(i);
            System.out.println(planStartTime);
            System.out.println(DateFormatUtil.getString2Date(subPlanEntity.getStartDateStr()));
            System.out.println(DateFormatUtil.getString2Date(subPlanEntity.getEndDateStr()));
            if (planStartTime >= DateFormatUtil.getString2Date(subPlanEntity.getStartDateStr()) &&
                    planStartTime < DateFormatUtil.getString2Date(subPlanEntity.getEndDateStr())) {
                subPlanEntity.setPlanStatus(1);
                currentEntity = new TbSubPlan();
                currentEntity.setKeyId(subPlanEntity.getKeyId());
                currentEntity.setLoad(subPlanEntity.getLoad());
                currentEntity.setPlanStatus(subPlanEntity.getPlanStatus());
                currentEntity.setModifyStatus(subPlanEntity.getModifyStatus());
                currentEntity.setTrainStep(subPlanEntity.getTrainStep());
                currentEntity.setTrainTime(subPlanEntity.getTrainTime());
                currentEntity.setClassId(subPlanEntity.getClassId());
                currentEntity.setDayNum(subPlanEntity.getDayNum());
                currentEntity.setEndDate(subPlanEntity.getEndDate());
                currentEntity.setId(subPlanEntity.getId());
                currentEntity.setPlanId(subPlanEntity.getPlanId());
                currentEntity.setStartDate(subPlanEntity.getStartDate());
                currentEntity.setUserId(subPlanEntity.getUserId());
                currentEntity.setWeekNum(subPlanEntity.getWeekNum());
                if ((i + 1) < subPlanEntityList.size()) {
                    nextEntity = subPlanEntityList.get(i + 1);
                }
                break;
            }
        }
        int dayCount;
        float diffWeek;
        if (currentEntity != null) {
            dayCount = Math.toIntExact((DateFormatUtil.getDateDiff(currentEntity.getStartDateStr(), currentEntity.getEndDateStr()) / dayMs));
            if (dayCount > 1 && nextEntity != null) {
                diffWeek = nextEntity.getLoad() - currentEntity.getLoad();
                float diff = diffWeek / dayCount;
                long startIntervalDate = planStartTime - DateFormatUtil.getString2Date(currentEntity.getStartDateStr());
                double currentDays = Math.ceil(startIntervalDate * 1.0f / dayMs);
                long value = Math.round((currentDays - 1) * diff);
                currentEntity.setLoad((int) (currentEntity.getLoad() + value));
                currentEntity.setKeyId(SnowflakeIdUtil.getUniqueId());

            }
            return currentEntity;
        }
        return null;
    }

    public boolean getUpLimitValue(int targetLoad, List<TbSubPlan> subPlanEntityList,long planStartTime) {
        TbSubPlan currentEntity = getThisDayLoadEntity(subPlanEntityList,planStartTime);
        return currentEntity.getLoad() > targetLoad;
    }

    public int calcInitLoad(int targetLoad, String date, String weight,long planStartTime, long userId) {
        int lowerLimit = -100;
        int upLimit = 100;
        for (int m = 0; m < 100; m++) {
            List<TbSubPlan> subPlanEntityList = insertTest(date, weight, userId, lowerLimit, 1, 6);
            try {
                if (getLowerLimitValue(targetLoad, subPlanEntityList,planStartTime)) {

                    break;
                }
            } catch (Exception e) {
                if (e.toString().contains("null object reference")) {
                    return Integer.MAX_VALUE;
                }
            }
            lowerLimit = lowerLimit - 50;
        }
        if (lowerLimit <= -5100) {
            return Integer.MAX_VALUE;
        }
        for (int m = 0; m < 100; m++) {
            List<TbSubPlan> subPlanEntityList = insertTest(date, weight, userId, upLimit, 1, 6);
            if (getUpLimitValue(targetLoad, subPlanEntityList,planStartTime)) {
                break;
            }
            upLimit = upLimit + 50;
        }
        int lastLoad = 0;
        for (int i = lowerLimit; i < upLimit; i++) {
            List<TbSubPlan> subPlanEntityList = insertTest(date, weight, userId, i, 1, 6);
            TbSubPlan currentEntity = getThisDayLoadEntity(subPlanEntityList,planStartTime);
            if (lastLoad < currentEntity.getLoad() && currentEntity.getLoad() >= targetLoad) {
                return i;
            }
        }

        return 0;
    }

    public List<TbSubPlan> insertTest(String startDate, String weight, long userId, int loadWeight, int classId, int weekTotal) {
        int diff = Integer.parseInt(weight) - loadWeight;
//        int diff = Integer.parseInt(SPHelper.getUser().getWeight()) - (int)SPHelper.getUserEvaluateWeight();
        List<TbSubPlan> subPlanEntityList = new ArrayList<>();
        for (int i = 0; i < weekTotal; i++) {
            TbSubPlan subPlanEntity = new TbSubPlan();
            subPlanEntity.setKeyId(SnowflakeIdUtil.getUniqueId());
            subPlanEntity.setClassId(classId);
            subPlanEntity.setUserId(userId);
            subPlanEntity.setLoad(loadWeight + diff * i / (weekTotal - 1));
            subPlanEntity.setWeekNum(i + 1);
            subPlanEntity.setDayNum(0);
            subPlanEntity.setPlanStatus(0);
            subPlanEntity.setStartDateStr(DateFormatUtil.getBeforeOrAfterDate(i * 7, startDate));
            if (i == weekTotal - 1) {
                subPlanEntity.setEndDateStr(DateFormatUtil.increaseOneDayOneSecondLess((i + 1) * 7 - 1, startDate));
            } else {
                subPlanEntity.setEndDateStr(DateFormatUtil.getBeforeOrAfterDate((i + 1) * 7, startDate));
            }


//            subPlanEntityDao.insertOrReplace(subPlanEntity);
            subPlanEntityList.add(subPlanEntity);
        }
        return subPlanEntityList;
    }
    public static int getPlanNum(String treatmentMethodId){
        switch (treatmentMethodId){
            case "请选择":
                return 0;
            case "6"://全髋关节置换术（THA）
            case "8"://全髋关节置换术（THA）
            case "19"://全髋关节置换术（THA）
            case "20"://半髋关节置换术
                return 1;
            case "39"://全膝关节置换术（TKA）
            case "40"://膝关节单髁置换术
            case "41"://膝关节融合术
                return 2;
            case "17"://空心钉内固定术
            case "18"://FNS内固定术
                return 3;
            case "21"://髓内钉内固定术
            case "22"://钢板内固定术
                return 4;
            case "43"://外固定支架固定术
                return 5;
            case "42"://切开复位钢板内固定术（ORIF）
                return 6;
            case "胫骨中段骨折（石膏固定）":
                return 7;
            case "46"://切开复位髓内钉固定术
                return 8;
            case "47"://骨不连翻修钢板内固定术（ORIF）
                return 9;
            case "7"://髋臼周围截骨术
                return 10;
            case "49"://踝关节骨折钢板螺钉内固定术
            case "50"://钢板螺钉内固定术
                return 11;
            case "56"://切开复位钢板内固定术（ORIF）
            case "57"://微创螺钉固定术
                return 12;
            case "51"://踝部韧带急性损伤修复与重建术
                return 13;
            case "10"://带血管游离腓骨移植术
                return 14;
            case "1"://外固定支架
            case "2"://切开复位内固定术（ORIF）
            case "3"://切开复位内固定术（ORIF）
            case "4"://切开复位内固定术（ORIF）
            case "9"://髓芯减压术
            case "12"://病灶清除松质骨植骨术
            case "23"://股骨近端截骨钢板内固定术
            case "24"://股骨近端截骨髓内钉内固定术
            case "25"://切开复位钢板内固定术（ORIF）
            case "26"://切开复位髓内钉内固定术（ORIF）
            case "27"://股骨干骨不连动力化术
            case "28"://股骨干骨不连翻修钢板内固定术
            case "29"://切开复位髓内钉内固定术（ORIF）
            case "30"://切开复位钢板内固定术（ORIF）
            case "31"://切开复位髓内钉内固定术
            case "32"://截骨矫形钢板内固定术（ORIF）
            case "33"://髌骨骨折切开复位张力带内固定术
            case "38"://胫骨结节移位术
            case "44"://截骨矫形钢板内固定术（ORIF）
            case "45"://切开复位钢板内固定术（ORIF）
            case "48"://外固定支架术
            case "52"://踝关节融合术
            case "58"://切开复位钢板内固定术（ORIF）
            case "59"://微创螺钉固定术
            case "60"://切开复位钢板内固定术（ORIF）
            case "61"://切开复位克氏针内固定术（ORIF）
            case "62"://切开复位钢板内固定术（ORIF）
            case "63"://克氏针内固定术
            case "64"://关节融合术
            case "65"://扁平足矫正钢板内固定术
            case "66"://踇内、外翻矫正钢板内固定术
                return 15;//四个月的训练计划
            case "5"://切开复位内固定术（ORIF）
            case "11"://带血管蒂髂骨移植术
                return 16;//6个月的训练计划
            case "13"://髋关节镜下修复术
            case "14"://髋关节镜下微骨折术
            case "15"://肋软骨移植术
            case "34"://膝关节韧带损伤修复与重建术
            case "35"://膝关节镜下半月板修整/成形术
            case "36"://关节镜下膝关节软骨微骨折术
            case "37"://软骨损伤移植/修复术
            case "53"://肌腱断裂修复术
            case "54"://关节镜下膝关节软骨微骨折术
            case "55"://软骨损伤移植/修复术
                return 17;//8周的训练计划
            case "16"://髋关节置换术后假体翻修术
                return 18;//6周的训练计划
            default:
                return 1012;

        }
    }
    public static int getDiagnosticNum(String diag) {
        switch (diag) {
            case "请选择":
                return 0;
            case "全髋关节置换":
                return 1;
            case "全膝关节置换":
                return 2;
            case "股骨颈骨折":
                return 3;
            case "股骨转子间骨折":
                return 4;
            case "胫骨平台骨折（钢板固定）":
                return 5;
            case "胫骨平台骨折（钢板内固定）":
                return 6;
            case "胫骨中段骨折（石膏固定）":
                return 7;
            case "胫骨中段骨折（髓内钉）":
                return 8;
            case "胫骨中段骨折（桥接钢板）":
                return 9;
            case "髋关节截骨术":
                return 10;
            case "踝关节骨折（钢板内固定）":
                return 11;
            case "跟骨骨折（钢板固定）":
                return 12;
            case "踝关节韧带损伤（踝关节韧带重建术）":
                return 13;
            case "股骨头坏死（腓骨移植术）":
                return 14;
            default:
                return 15;

        }
    }
}
