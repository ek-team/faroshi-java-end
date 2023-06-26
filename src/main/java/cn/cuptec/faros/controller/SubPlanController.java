package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.service.DoctorUpdateSubPlanRecordService;
import cn.cuptec.faros.service.PlanService;
import cn.cuptec.faros.service.PlanUserService;
import cn.cuptec.faros.service.SubPlanService;
import cn.cuptec.faros.util.SnowflakeIdWorker;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/subPlan")
@Slf4j
public class SubPlanController extends AbstractBaseController<SubPlanService, TbSubPlan> {

    @Resource
    private PlanService planService;
    @Resource
    private PlanUserService planUserService;
    @Resource
    private DoctorUpdateSubPlanRecordService doctorUpdateSubPlanRecordService;

    public static void main(String[] args) {


        int step = 10;
        int week = 4;
        int stepllll = 200 / week;
        for (int i = 0; i < week; i++) {
            if (i == 0) {
                System.out.println(step);
            } else {
                step = step + stepllll;
                System.out.println(step);

            }

        }
    }

    public static int daysBetween(Date date1, Date date2) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(date1);
        long time1 = cal.getTimeInMillis();
        cal.setTime(date2);
        long time2 = cal.getTimeInMillis();
        long between_days = (time2 - time1) / (1000 * 3600 * 24);
        Integer day = Integer.parseInt(String.valueOf(between_days));
        Double week = day.doubleValue() / 7;
        if (isInt(week)) {
            return week.intValue();
        }
        return week.intValue() + 1;
    }

    public static boolean isInt(double a) {
        double b = a;
        int b1 = (int) a;
        if (b % b1 == 0)
            return true;
        else
            return false;
    }

    @GetMapping("/addInitSubPlanTest")
    public RestResponse addInitSubPlanTest() {
        Page<TbSubPlan> page = new Page<>();
        page.setSize(1);
        page.setCurrent(1);
        QueryWrapper queryWrapper = new QueryWrapper<TbSubPlan>();
        queryWrapper.eq("user_id", "1630011645376417792");
        queryWrapper.orderByAsc("version");
        IPage page1 = service.page(page, queryWrapper);
        List<TbSubPlan> subPlans = page1.getRecords();
        Integer version = 1;
        if (!CollectionUtils.isEmpty(subPlans)) {
            version = subPlans.get(0).getVersion() + 1;
        }
        System.out.println(version);
        return RestResponse.ok();
    }

    /**
     * 添加初始计划
     *
     * @return
     */
    @PostMapping("/addInitSubPlan")
    public RestResponse addInitSubPlan(@RequestBody TbSubPlan subPlanEntity) {
        Long userId = subPlanEntity.getUserId();
        Page<TbSubPlan> page = new Page<>();
        page.setSize(1);
        page.setCurrent(1);
        QueryWrapper queryWrapper = new QueryWrapper<TbSubPlan>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByDesc("version");
        IPage page1 = service.page(page, queryWrapper);
        List<TbSubPlan> subPlans = page1.getRecords();
        Integer version = 1;
        if (!CollectionUtils.isEmpty(subPlans)) {
            version = subPlans.get(0).getVersion() + 1;
        }

        Date startDate = subPlanEntity.getStartDate();
        Date endDate = subPlanEntity.getEndDate();
        Integer startLoad = subPlanEntity.getLoad();
        Integer endLoad = subPlanEntity.getEndLoad();
        Integer weeks = daysBetween(startDate, endDate);
        List<TbSubPlan> tbSubPlans = new ArrayList<>();
        SnowflakeIdWorker idUtil = new SnowflakeIdWorker(0, 0);
        TbPlan tbPlan = new TbPlan();
        tbPlan.setPlanId(idUtil.nextId());
        tbPlan.setKeyId(idUtil.nextId());
        tbPlan.setUserId(subPlanEntity.getUserId());
        tbPlan.setStartDate(startDate);
        tbPlan.setEndDate(endDate);
        tbPlan.setWeight(subPlanEntity.getWeight());
        tbPlan.setUpdateDate(new Date());
        planService.save(tbPlan);
        Integer trainStep = subPlanEntity.getTrainStep();//踩踏次数
        Integer load = endLoad - startLoad;
        if (weeks == 0) {
            weeks = 1;
        }
        Integer weekLOad = load / weeks;//要改
        Integer loadData = startLoad;
        Integer startDay = 0;
        Integer endDay = 7;

        if (weeks > 1) {

            int step = 200 / weeks;
            for (int i = 0; i < weeks; i++) {
                TbSubPlan tbSubPlan = new TbSubPlan();
                if (i == 0) {
                    tbSubPlan.setTrainStep(trainStep);
                } else {
//                    if (i == weeks - 1) {
//                        tbSubPlan.setTrainStep(200);
//
//                    } else {
//                        trainStep = trainStep + step;
//                        tbSubPlan.setTrainStep(trainStep);
//                    }
                    tbSubPlan.setTrainStep(((i + 1) * 200 + (weeks - (i + 1)) * trainStep) / weeks);

                }
                tbSubPlan.setPlanId(tbPlan.getPlanId());
                tbSubPlan.setWeekNum(i + 1);
                tbSubPlan.setDayNum(1);
                tbSubPlan.setCreateDate(new Date());
                tbSubPlan.setInitStart(1);
                tbSubPlan.setUpdateDate(new Date());
                tbSubPlan.setKeyId(idUtil.nextId());
                tbSubPlan.setVersion(version);
                tbSubPlan.setUserId(subPlanEntity.getUserId());
//                if (i == weeks - 1) {
//                    tbSubPlan.setLoad(endLoad);
//                } else {
//                    tbSubPlan.setLoad(loadData);
//                    loadData = loadData + weekLOad;
//                }
                int weekLoad = (((i + 1) * endLoad) + (weeks - (i + 1)) * startLoad) / weeks;
                tbSubPlan.setLoad(weekLoad);
                if (i == 0) {
                    tbSubPlan.setStartDate(addAndSubtractDaysByGetTime(startDate, startDay));
                    tbSubPlan.setEndDate(addAndSubtractDaysByGetTime(startDate, endDay));
                } else {
                    tbSubPlan.setStartDate(addAndSubtractDaysByGetTime(startDate, startDay));
                    if (i == weeks - 1) {
                        tbSubPlan.setEndDate(endDate);

                    } else {
                        tbSubPlan.setEndDate(addAndSubtractDaysByGetTime(startDate, endDay));

                    }
                }
                startDay = startDay + 7;
                endDay = endDay + 7;


                tbSubPlans.add(tbSubPlan);
            }
        } else {
            subPlanEntity.setPlanId(tbPlan.getPlanId());
            subPlanEntity.setTrainStep(trainStep);
            subPlanEntity.setWeekNum(1);
            subPlanEntity.setDayNum(1);
            subPlanEntity.setCreateDate(new Date());
            subPlanEntity.setInitStart(1);
            subPlanEntity.setVersion(version);
            subPlanEntity.setUpdateDate(new Date());
            subPlanEntity.setKeyId(idUtil.nextId());
            tbSubPlans.add(subPlanEntity);

        }
        service.saveBatch(tbSubPlans);
        return RestResponse.ok();
    }

    @PostMapping("/saveOrUpdate")
    public RestResponse saveOrUpdate(@RequestBody List<TbSubPlan> subPlanEntity) {
        service.remove(new QueryWrapper<TbSubPlan>().lambda().eq(TbSubPlan::getUserId, subPlanEntity.get(0).getUserId()));
        String url = "http://pharos.ewj100.com/subPlan/listByUserId?userId=" + subPlanEntity.get(0).getUserId();
        String post = HttpUtil.get(url);
        List<TbSubPlan> objectList = JSONObject.parseArray(post, TbSubPlan.class);
        log.info("kaskdkkkkkkk" + objectList.toString());
        List<TbSubPlan> list1 = new ArrayList<>();
        for (TbSubPlan tbSubPlan : objectList) {

            TbSubPlan newTbSubPlan = new TbSubPlan();
            BeanUtils.copyProperties(tbSubPlan, newTbSubPlan, "id");

            list1.add(newTbSubPlan);
        }

        service.saveBatch(list1);

        String url1 = "http://pharos.ewj100.com/subPlan/listPlanByUserId?userId=" + subPlanEntity.get(0).getUserId();
        String post1 = HttpUtil.get(url1);
        List<TbPlan> tbPlans = JSONObject.parseArray(post1, TbPlan.class);
        List<TbPlan> list2 = new ArrayList<>();
        for (TbPlan tbPlan : tbPlans) {

            TbPlan newTbPlan = new TbPlan();
            BeanUtils.copyProperties(tbPlan, newTbPlan, "id");

            list2.add(newTbPlan);
        }
        planService.remove(new QueryWrapper<TbPlan>().lambda().eq(TbPlan::getUserId, subPlanEntity.get(0).getUserId()));
        planService.saveBatch(list2);

        return RestResponse.ok();
    }

    @GetMapping("/listByUserId")
    public List<TbSubPlan> listByUserId(@RequestParam("userId") String userId) {

        return service.list(new QueryWrapper<TbSubPlan>().lambda().eq(TbSubPlan::getUserId, userId));
    }
    @GetMapping("/listPlanByUserId")
    public List<TbPlan> listPlanByUserId(@RequestParam("userId") String userId) {

        return planService.list(new QueryWrapper<TbPlan>().lambda().eq(TbPlan::getUserId, userId));
    }
    @PostMapping("/update")
    public RestResponse<TbSubPlan> update(@RequestBody List<TbSubPlan> subPlanEntity) {

        log.info(subPlanEntity.size() + "同步数据====================");
        log.info(subPlanEntity.toString());


        Date sortEndDate = null;
        Integer version = 1;
        for (TbSubPlan tbSubPlan : subPlanEntity) {
            if (tbSubPlan.getVersion() != null) {
                version = tbSubPlan.getVersion() + 1;
                break;
            }
        }
        SnowflakeIdWorker idUtil = new SnowflakeIdWorker(0, 0);

        Integer classId = 1;
        Integer trainStep = 1;
        Integer trainTime = 30;
        List<TbSubPlan> tbSubPlansTime = new ArrayList<>();
        for (TbSubPlan tbSubPlan : subPlanEntity) {
            if (tbSubPlan.getClassId() != null && tbSubPlan.getClassId() > classId) {
                classId = tbSubPlan.getClassId();

            }
            if (tbSubPlan.getId() != null) {
                sortEndDate = tbSubPlan.getEndDate();
            }
            if (tbSubPlan.getEndDate() != null) {
                tbSubPlansTime.add(tbSubPlan);
                sortEndDate = tbSubPlan.getEndDate();
            }
            tbSubPlan.setVersion(version);
            tbSubPlan.setUpdateDate(new Date());

            if (tbSubPlan.getKeyId() == null) {
                long hId = idUtil.nextId();

                tbSubPlan.setKeyId(hId);
            }
            if (tbSubPlan.getClassId() == null) {
                tbSubPlan.setClassId(classId);
            }
            if (tbSubPlan.getTrainStep() == null) {
                tbSubPlan.setTrainStep(trainStep);
            } else {
                if (tbSubPlan.getTrainStep() > trainStep) {
                    trainStep = tbSubPlan.getTrainStep();

                }
            }
            if (tbSubPlan.getTrainTime() == null) {
                tbSubPlan.setTrainTime(trainTime);
            } else {
                if (tbSubPlan.getTrainTime() > trainTime) {
                    trainTime = tbSubPlan.getTrainTime();

                }
            }
        }


        //判断没有id的添加
        List<TbSubPlan> tbSubPlans = new ArrayList<>();
        if (!CollectionUtils.isEmpty(tbSubPlansTime)) {
            tbSubPlansTime.sort((t1, t2) -> t2.getEndDate().compareTo(t1.getEndDate()));
            sortEndDate = tbSubPlansTime.get(0).getEndDate();
        }
        if (sortEndDate == null) {
            //查询第一个版本的数据获取时间
            List<TbSubPlan> list = service.list(new QueryWrapper<TbSubPlan>().lambda().eq(TbSubPlan::getUserId, subPlanEntity.get(0).getUserId()).orderByDesc(TbSubPlan::getEndDate));
            if (!CollectionUtils.isEmpty(list)) {
                sortEndDate = list.get(0).getEndDate();
            }

            //代表之前的记录都没有时间 要将这次传的数据 都加上时间
            for (int i = 0; i < subPlanEntity.size(); i++) {
                TbSubPlan tbSubPlan = list.get(i);
                if (tbSubPlan != null) {
                    subPlanEntity.get(i).setStartDate(tbSubPlan.getStartDate());
                    subPlanEntity.get(i).setEndDate(tbSubPlan.getEndDate());
                } else if (sortEndDate != null) {
                    subPlanEntity.get(i).setStartDate(sortEndDate);
                    subPlanEntity.get(i).setEndDate(addAndSubtractDaysByGetTime(sortEndDate, 7));
                    sortEndDate = subPlanEntity.get(i).getEndDate();
                }


            }
        }
        service.updateBatchById(subPlanEntity);

        for (TbSubPlan tbSubPlan : subPlanEntity) {
            tbSubPlan.setUpdateDate(new Date());
            if (tbSubPlan.getId() == null) {
//                if (sortEndDate != null) {
//                    tbSubPlan.setStartDate(sortEndDate);
//                    tbSubPlan.setEndDate(addAndSubtractDaysByGetTime(sortEndDate, 7));
//                    sortEndDate = tbSubPlan.getEndDate();
//                }

                tbSubPlans.add(tbSubPlan);
            }
        }
        if (!CollectionUtils.isEmpty(tbSubPlans)) {
            service.saveBatch(tbSubPlans);
        }
        Date endDate;
        if (CollectionUtils.isEmpty(tbSubPlans)) {
            endDate = subPlanEntity.get(subPlanEntity.size() - 1).getEndDate();
        } else {
            endDate = tbSubPlans.get(tbSubPlans.size() - 1).getEndDate();
        }
        LambdaUpdateWrapper<TbPlan> wrapper = new UpdateWrapper<TbPlan>().lambda()
                .set(TbPlan::getUpdateDate, new Date())
                .set(TbPlan::getEndDate, endDate)
                .eq(TbPlan::getUserId, subPlanEntity.get(0).getUserId());
        planService.update(wrapper);

        if (subPlanEntity.get(0).getUpdateStatus() == null) {
            List<TbSubPlan> list = service.list(new QueryWrapper<TbSubPlan>().lambda().eq(TbSubPlan::getUserId, subPlanEntity.get(0).getUserId()));


            String url = "http://pharos.ewj100.com/subPlan/saveOrUpdate"; //同步到老平台
            String params = JSONObject.toJSONString(list);
            String post = HttpUtil.post(url, params);
        }
        return RestResponse.ok();
    }

    public static Date addAndSubtractDaysByGetTime(Date dateTime/*待处理的日期*/, int n/*加减天数*/) {

        //日期格式
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat dd = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        System.out.println(df.format(new Date(dateTime.getTime() + n * 24 * 60 * 60 * 1000L)));
        //System.out.println(dd.format(new Date(dateTime.getTime() + n * 24 * 60 * 60 * 1000L)));
        //注意这里一定要转换成Long类型，要不n超过25时会出现范围溢出，从而得不到想要的日期值
        return new Date(dateTime.getTime() + n * 24 * 60 * 60 * 1000L);
    }

    /**
     * 批量修改
     *
     * @param subPlanEntity
     * @return
     */
    @PostMapping("/batchUpdate")
    public RestResponse<TbSubPlan> batchUpdate(@RequestBody List<TbSubPlan> subPlanEntity) {

        if (!CollectionUtils.isEmpty(subPlanEntity)) {
            for (TbSubPlan tbSubPlan : subPlanEntity) {
                tbSubPlan.setUpdateDate(new Date());
            }
            service.updateBatchById(subPlanEntity);

            if (subPlanEntity.get(0).getUpdateStatus() == null) {

                String url = "http://pharos.ewj100.com/subPlan/saveOrUpdate";
                String params = JSONObject.toJSONString(subPlanEntity);
                String post = HttpUtil.post(url, params);
            }


        }
        return RestResponse.ok();
    }

    @PostMapping("/delete")
    public RestResponse<TbSubPlan> delete(@RequestBody List<TbSubPlan> subPlanEntity) {
        // service.remove(new QueryWrapper<TbSubPlan>().lambda().eq(TbSubPlan::getUserId, subPlanEntity.get(0).getUserId()));
        Integer version = 1;
        for (TbSubPlan tbSubPlan : subPlanEntity) {
            if (tbSubPlan.getVersion() != null) {
                version = tbSubPlan.getVersion() + 1;
                break;
            }
        }
        for (TbSubPlan tbSubPlan : subPlanEntity) {
            tbSubPlan.setVersion(version);
            tbSubPlan.setUpdateDate(new Date());
        }
        LambdaUpdateWrapper<TbPlan> wrapper = new UpdateWrapper<TbPlan>().lambda()
                .set(TbPlan::getUpdateDate, new Date())
                .set(TbPlan::getEndDate, subPlanEntity.get(subPlanEntity.size() - 1).getEndDate())
                .eq(TbPlan::getUserId, subPlanEntity.get(0).getUserId());
        planService.update(wrapper);
        service.saveBatch(subPlanEntity);
        return RestResponse.ok();
    }

    @PostMapping("/addHistory")
    public RestResponse<TbSubPlan> addHistory(@RequestBody List<TbSubPlan> subPlanEntity) {
        for (TbSubPlan tbSubPlan : subPlanEntity) {
            tbSubPlan.setUpdateDate(new Date());
            tbSubPlan.setNewStatus(1);
        }
//        String url = "http://pharos.ewj100.com/subPlan/addHistory";
//        String params = JSONObject.toJSONString(subPlanEntity);
//        String post = HttpUtil.post(url, params);

        LambdaUpdateWrapper<TbPlan> wrapper = new UpdateWrapper<TbPlan>().lambda()
                .set(TbPlan::getUpdateDate, new Date())
                .eq(TbPlan::getUserId, subPlanEntity.get(0).getUserId());
        planService.update(wrapper);
        service.saveBatch(subPlanEntity);
        return RestResponse.ok();
    }

    @GetMapping("/old")
    public RestResponse<List<TbSubPlan>> old(@RequestParam("userId") String userId, @RequestParam("version") Integer version) {

        Page<TbSubPlan> page = new Page<>();
        page.setSize(1);
        page.setCurrent(1);
        QueryWrapper queryWrapper = new QueryWrapper<TbSubPlan>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByAsc("version");
        IPage page1 = service.page(page, queryWrapper);
        List<TbSubPlan> subPlans = page1.getRecords();
        if (!CollectionUtils.isEmpty(subPlans)) {
            subPlans = service.list(new QueryWrapper<TbSubPlan>().lambda().eq(TbSubPlan::getVersion, subPlans.get(0).getVersion()).eq(TbSubPlan::getUserId, userId));

        }
        log.info("========撒打算打算打算的" + userId + "asdasdasdasd" + version);
        version = version + 1;
        if (!CollectionUtils.isEmpty(subPlans)) {
            for (TbSubPlan tbSubPlan : subPlans) {
                tbSubPlan.setVersion(version);
                tbSubPlan.setUpdateDate(new Date());
            }
            service.saveBatch(subPlans);
        }
        LambdaUpdateWrapper<TbPlan> wrapper = new UpdateWrapper<TbPlan>().lambda()
                .set(TbPlan::getUpdateDate, new Date())
                .eq(TbPlan::getUserId, userId);
        planService.update(wrapper);
        return RestResponse.ok(subPlans);
    }

    @GetMapping("/oldIdCard")
    public RestResponse<List<TbSubPlan>> oldIdCard(@RequestParam("idCard") String idCard, @RequestParam("version") Integer version) {

        List<TbTrainUser> list = planUserService.list(new QueryWrapper<TbTrainUser>().lambda().eq(TbTrainUser::getIdCard, idCard));
        if (CollectionUtils.isEmpty(list)) {
            return RestResponse.ok();
        }
        String userId = list.get(0).getUserId();
        Page<TbSubPlan> page = new Page<>();
        page.setSize(1);
        page.setCurrent(1);
        QueryWrapper queryWrapper = new QueryWrapper<TbSubPlan>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByAsc("version");
        IPage page1 = service.page(page, queryWrapper);
        List<TbSubPlan> subPlans = page1.getRecords();
        if (!CollectionUtils.isEmpty(subPlans)) {
            subPlans = service.list(new QueryWrapper<TbSubPlan>().lambda().eq(TbSubPlan::getVersion, subPlans.get(0).getVersion()).eq(TbSubPlan::getUserId, userId));

        }
        version = version + 1;
        if (!CollectionUtils.isEmpty(subPlans)) {
            for (TbSubPlan tbSubPlan : subPlans) {
                tbSubPlan.setVersion(version);
                tbSubPlan.setUpdateDate(new Date());
            }
            service.saveBatch(subPlans);
        }
        LambdaUpdateWrapper<TbPlan> wrapper = new UpdateWrapper<TbPlan>().lambda()
                .set(TbPlan::getUpdateDate, new Date())
                .eq(TbPlan::getUserId, userId);
        planService.update(wrapper);
        return RestResponse.ok(subPlans);
    }

    @GetMapping("/getOne")
    public RestResponse<List<TbSubPlan>> getOne(@RequestParam("userId") String userId) {
        Page<TbSubPlan> page = new Page<>();
        page.setSize(1);
        page.setCurrent(1);
        QueryWrapper queryWrapper = new QueryWrapper<TbSubPlan>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByAsc("version");
        IPage page1 = service.page(page, queryWrapper);
        List<TbSubPlan> subPlans = page1.getRecords();
        if (!CollectionUtils.isEmpty(subPlans)) {
            subPlans = service.list(new QueryWrapper<TbSubPlan>().lambda().eq(TbSubPlan::getVersion, subPlans.get(0).getVersion()).eq(TbSubPlan::getUserId, userId));

        }

        return RestResponse.ok(subPlans);
    }
    @GetMapping("/updatePlanLnValid")
    public RestResponse<List<TbSubPlan>> updatePlanLnValid(@RequestParam("userId") String userId,@RequestParam("planInvalid") Integer planInvalid) {
        Page<TbSubPlan> page = new Page<>();
        page.setSize(1);
        page.setCurrent(1);
        QueryWrapper queryWrapper = new QueryWrapper<TbSubPlan>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByAsc("version");
        IPage page1 = service.page(page, queryWrapper);
        List<TbSubPlan> subPlans = page1.getRecords();
        if (!CollectionUtils.isEmpty(subPlans)) {
           for(TbSubPlan tbSubPlan:subPlans){
               tbSubPlan.setPlanInvalid(planInvalid);
           }
            service.updateBatchById(subPlans);
        }

        return RestResponse.ok(subPlans);
    }
    @GetMapping("/getOneByIdCard")
    public RestResponse<List<TbSubPlan>> getOneByIdCard(@RequestParam(value = "idCard", required = false) String idCard, @RequestParam(value = "xtUserId", required = false) String xtUserId) {

        LambdaQueryWrapper<TbTrainUser> eq = new QueryWrapper<TbTrainUser>().lambda();
        if (!StringUtils.isEmpty(idCard)) {
            eq.eq(TbTrainUser::getIdCard, idCard);
        } else if (!StringUtils.isEmpty(xtUserId)) {
            eq.eq(TbTrainUser::getXtUserId, xtUserId);
        }

        List<TbTrainUser> list = planUserService.list(eq);
        if (CollectionUtils.isEmpty(list)) {
            return RestResponse.ok();
        }
        String userId = list.get(0).getUserId();

        Page<TbSubPlan> page = new Page<>();
        page.setSize(1);
        page.setCurrent(1);
        QueryWrapper queryWrapper = new QueryWrapper<TbSubPlan>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByAsc("version");
        IPage page1 = service.page(page, queryWrapper);
        List<TbSubPlan> subPlans = page1.getRecords();
        if (!CollectionUtils.isEmpty(subPlans)) {
            subPlans = service.list(new QueryWrapper<TbSubPlan>().lambda().eq(TbSubPlan::getVersion, subPlans.get(0).getVersion()).eq(TbSubPlan::getUserId, userId));

        }

        return RestResponse.ok(subPlans);
    }

    //查询历史记录

    @GetMapping("/getHistoryByUserId")
    public RestResponse<Map<String, List<TbSubPlan>>> getHistoryByUserId(@RequestParam("userId") String userId) {

        Page<TbSubPlan> page = new Page<>();
        page.setSize(1);
        page.setCurrent(1);
        QueryWrapper queryWrapper = new QueryWrapper<TbSubPlan>();
        queryWrapper.eq("user_id", userId);
        queryWrapper.orderByDesc("version");
        IPage page1 = service.page(page, queryWrapper);
        List<TbSubPlan> subPlans = page1.getRecords();
        if (!CollectionUtils.isEmpty(subPlans)) {
            subPlans = service.list(new QueryWrapper<TbSubPlan>().lambda().ne(TbSubPlan::getVersion, subPlans.get(0).getVersion()).eq(TbSubPlan::getUserId, userId));

        }
        if (!CollectionUtils.isEmpty(subPlans)) {
            Map<String, List<TbSubPlan>> map = new HashMap<>();
            for (TbSubPlan tbSubPlan : subPlans) {
                List<TbSubPlan> tbSubPlans = map.get(tbSubPlan.getVersion() + "");
                if (CollectionUtils.isEmpty(tbSubPlans)) {
                    tbSubPlans = new ArrayList<>();
                    tbSubPlans.add(tbSubPlan);
                } else {
                    tbSubPlans.add(tbSubPlan);
                }
                map.put(tbSubPlan.getVersion() + "", tbSubPlans);
            }

            return RestResponse.ok(map);
        }

        return RestResponse.ok();
    }

    @PostMapping("/add")
    public RestResponse add(@RequestBody AddTbSubPlanParam param) {
        for (TbSubPlan tbSubPlan : param.getTbSubPlans()) {
            tbSubPlan.setCreateDate(new Date());
            tbSubPlan.setInitStart(1);
        }
        service.saveBatch(param.getTbSubPlans());
        return RestResponse.ok();
    }

    //添加医生修改计划 记录
    @GetMapping("/addDoctorUpdateSubPlanRecord")
    public RestResponse addDoctorUpdateSubPlanRecord(@RequestParam("doctorName") String doctorName,
                                                     @RequestParam("userId") String userId,
                                                     @RequestParam("beforeVersion") Integer beforeVersion,
                                                     @RequestParam("afterVersion") Integer afterVersion) {
        String url = "http://pharos.ewj100.com/subPlan/batchUpdate?doctorName=" + doctorName + "&userId=" + userId + "&beforeVersion=" + beforeVersion + "&afterVersion=" + afterVersion;
        String post = HttpUtil.get(url);

        DoctorUpdateSubPlanRecord record = new DoctorUpdateSubPlanRecord();
        record.setDoctorName(doctorName);
        record.setUserId(userId);
        record.setAfterVersion(afterVersion);
        record.setBeforeVersion(beforeVersion);
        record.setCreateTime(new Date());
        doctorUpdateSubPlanRecordService.save(record);
        return RestResponse.ok();
    }

    /**
     * 根据userid查询用户的计划负重 根据时间排序
     *
     * @return
     */
    @GetMapping("/querySubPlanByUserId")
    public RestResponse querySubPlanByUserId(@RequestParam("userId") String userId) {

        List<TbSubPlan> list = service.list(new QueryWrapper<TbSubPlan>().lambda()
                .eq(TbSubPlan::getUserId, userId).orderByAsc(TbSubPlan::getStartDate));
        if (CollectionUtils.isEmpty(list)) {
            return RestResponse.ok(list);
        }
        List<TbSubPlan> tbSubPlans = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            TbSubPlan tbSubPlan = list.get(i);//当前周
            TbSubPlan tbSubPlanNext = list.get(i + 1);//下一周
            Integer load = tbSubPlanNext.getLoad() - tbSubPlan.getLoad();
            Integer dayLoad = load / 7;
            for (int j = 0; j < 7; j++) {
                TbSubPlan tbSubPlan1 = new TbSubPlan();
                tbSubPlan1.setLoad(tbSubPlan.getLoad() + dayLoad);
                tbSubPlans.add(tbSubPlan1);
            }


        }
        return RestResponse.ok(tbSubPlans);

    }

    @Override
    protected Class<TbSubPlan> getEntityClass() {
        return TbSubPlan.class;
    }
}
