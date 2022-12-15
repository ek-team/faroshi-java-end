package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.dto.AddPneumaticPlan;
import cn.cuptec.faros.entity.PneumaticPlan;
import cn.cuptec.faros.entity.PneumaticRecord;
import cn.cuptec.faros.entity.ProductStock;
import cn.cuptec.faros.service.PneumaticPlanService;
import cn.cuptec.faros.service.PneumaticRecordService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/pneumaticPlan")
@Slf4j
public class PneumaticPlanController extends AbstractBaseController<PneumaticPlanService, PneumaticPlan> {

    /**
     * 添加计划
     *
     * @return
     */
    @PostMapping("/save")
    public RestResponse save(@RequestBody AddPneumaticPlan param) {
        if (CollectionUtils.isEmpty(param.getDatas())) {
            return RestResponse.ok();
        }
        List<PneumaticPlan> pneumaticPlanList = param.getDatas();
        String userId = null;
        List<PneumaticPlan> addData = new ArrayList<>();
        Integer sort = 0;
        for (PneumaticPlan pneumaticPlan : pneumaticPlanList) {
            LocalDateTime updateTime = pneumaticPlan.getUpdateTime();
            String startWeekTime = pneumaticPlan.getStartWeekTime();

            List<PneumaticPlan> pneumaticPlanList1 = pneumaticPlan.getPneumaticPlanList();
//            List<PneumaticPlan> list = service.list(new QueryWrapper<PneumaticPlan>().lambda().eq(PneumaticPlan::getStartWeekTime, startWeekTime).eq(PneumaticPlan::getUserId, pneumaticPlan.getUserId()));
//            if (!CollectionUtils.isEmpty(list)) {
//                for (PneumaticPlan plan : list) {
//                    Boolean a = true;
//                    for (PneumaticPlan plan1 : pneumaticPlanList1) {
//                        if (plan.getName().equals(plan1.getName()) && plan.getTime().equals(plan1.getTime())) {
//                            a = false;
//                        }
//
//                    }
//                    if (a) {
//                        service.removeById(plan.getId());
//                    }
//                }
//
//            }
            service.remove(new QueryWrapper<PneumaticPlan>().lambda().in(PneumaticPlan::getStartWeekTime, startWeekTime).eq(PneumaticPlan::getUserId, pneumaticPlan.getUserId()));

            for (PneumaticPlan pneumaticPlan2 : pneumaticPlanList1) {
                PneumaticPlan pneumaticPlan1 = new PneumaticPlan();
                pneumaticPlan1.setSort(sort);
                sort++;
                pneumaticPlan1.setStartWeekTime(startWeekTime);
                pneumaticPlan1.setEndWeekTime(pneumaticPlan.getEndWeekTime());
                pneumaticPlan1.setUserId(pneumaticPlan.getUserId());
                //  pneumaticPlan1.setDayTime(startWeek.plusDays(i));
                pneumaticPlan1.setTime(pneumaticPlan2.getTime());
                pneumaticPlan1.setName(pneumaticPlan2.getName());
                if (!StringUtils.isEmpty(pneumaticPlan.getUserId())) {
                    userId = pneumaticPlan.getUserId();
                }
                pneumaticPlan1.setUserId(userId);
                pneumaticPlan1.setPlanType(pneumaticPlan.getPlanType());
                if (pneumaticPlan2.getName().indexOf("手套操") >= 0) {
                    pneumaticPlan1.setType(16);
                }
                if (pneumaticPlan2.getName().indexOf("镜像") >= 0) {
                    pneumaticPlan1.setType(5);
                }
                if (pneumaticPlan2.getName().indexOf("抗阻") >= 0) {
                    pneumaticPlan1.setType(12);
                }
                if (pneumaticPlan2.getName().indexOf("助力") >= 0) {
                    pneumaticPlan1.setType(11);
                }
                if (pneumaticPlan2.getName().indexOf("游戏") >= 0) {
                    pneumaticPlan1.setType(2);
                }
                if (pneumaticPlan2.getName().indexOf("功能") >= 0) {
                    pneumaticPlan1.setType(7);
                }
                if (pneumaticPlan2.getName().indexOf("休息") >= 0) {
                    pneumaticPlan1.setType(0);
                }
                    pneumaticPlan1.setUpdateTime(LocalDateTime.now());
                    addData.add(pneumaticPlan1);


            }

        }
        List<String> deleteDate = new ArrayList<>();
        if (!CollectionUtils.isEmpty(addData)) {
            for (PneumaticPlan pneumaticPlan : addData) {
                deleteDate.add(pneumaticPlan.getStartWeekTime());
            }

        }
        if (!CollectionUtils.isEmpty(deleteDate)) {
            service.remove(new QueryWrapper<PneumaticPlan>().lambda().in(PneumaticPlan::getStartWeekTime, deleteDate).eq(PneumaticPlan::getUserId, userId));
        }
        if (!CollectionUtils.isEmpty(addData)) {
            service.saveBatch(addData);

        }

        return RestResponse.ok();
    }

    /**
     * 根据时间删除
     */
    @GetMapping("/deleteByTime")
    public RestResponse deleteByTime(@RequestParam("time") String time, @RequestParam("userId") String userId) {
        service.remove(new QueryWrapper<PneumaticPlan>().lambda().eq(PneumaticPlan::getStartWeekTime, time).eq(PneumaticPlan::getUserId, userId));

        return RestResponse.ok();
    }

    /**
     * 根据用户id查询气动训练计划
     *
     * @return
     */
    @GetMapping("/getByUserId")
    public RestResponse getByUserId(@RequestParam(required = false, value = "idCard") String idCard, @RequestParam(value = "userId", required = false) String userId) {
        List<PneumaticPlan> list;
        if (!StringUtils.isEmpty(idCard)) {
            list = service.list(new QueryWrapper<PneumaticPlan>().lambda()
                    .eq(PneumaticPlan::getIdCard, idCard));
        } else {
            list = service.list(new QueryWrapper<PneumaticPlan>().lambda()
                    .eq(PneumaticPlan::getUserId, userId));
        }


        if (CollectionUtils.isEmpty(list)) {
            //初始化json数据
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate time = LocalDate.now();
            String start = df.format(time);
            LocalDate localDate = time.plusDays(6);
            String end = df.format(localDate);
            //如果是空 默认 添加训练计划
            List<PneumaticPlan> pneumaticPlanList = new ArrayList<>();
            Integer sort = 0;
            for (int i = 0; i < 8; i++) {

                if (i == 0) {//计划1
                    for (int j = 0; j < 3; j++) {
                        PneumaticPlan pneumaticPlan = new PneumaticPlan();
                        pneumaticPlan.setUserId(userId);
                        pneumaticPlan.setStartWeekTime(start + " 00:00:00");
                        pneumaticPlan.setEndWeekTime(end);
                        pneumaticPlan.setPlanType(i + 1);
                        pneumaticPlan.setSort(sort);
                        sort++;
                        pneumaticPlan.setUpdateTime(LocalDateTime.now());
                        if (j == 0) {
                            pneumaticPlan.setName("手套操训练");
                            pneumaticPlan.setTime(40);
                            pneumaticPlan.setType(16);
                        }
                        if (j == 1) {
                            pneumaticPlan.setName("休息");
                            pneumaticPlan.setTime(40);
                            pneumaticPlan.setType(0);
                        }
                        if (j == 2) {
                            pneumaticPlan.setName("手套操训练");
                            pneumaticPlan.setTime(40);
                            pneumaticPlan.setType(16);
                        }
                        pneumaticPlanList.add(pneumaticPlan);
                    }

                }

                if (i == 1) {//计划2
                    time = time.plusDays(7);
                    start = df.format(time);
                    end = df.format(localDate);
                    for (int j = 0; j < 5; j++) {
                        PneumaticPlan pneumaticPlan = new PneumaticPlan();
                        pneumaticPlan.setUserId(userId);
                        pneumaticPlan.setStartWeekTime(start + " 00:00:00");
                        pneumaticPlan.setEndWeekTime(end);
                        pneumaticPlan.setPlanType(i + 1);
                        pneumaticPlan.setSort(sort);
                        sort++;
                        pneumaticPlan.setUpdateTime(LocalDateTime.now());
                        if (j == 0) {
                            pneumaticPlan.setName("手套操训练");
                            pneumaticPlan.setTime(30);
                            pneumaticPlan.setType(16);
                        }
                        if (j == 1) {
                            pneumaticPlan.setName("镜像训练");
                            pneumaticPlan.setTime(10);
                            pneumaticPlan.setType(5);
                        }
                        if (j == 2) {
                            pneumaticPlan.setName("休息");
                            pneumaticPlan.setTime(40);
                            pneumaticPlan.setType(0);
                        }
                        if (j == 3) {
                            pneumaticPlan.setName("手套操训练");
                            pneumaticPlan.setTime(30);
                            pneumaticPlan.setType(16);
                        }
                        if (j == 4) {
                            pneumaticPlan.setName("镜像训练");
                            pneumaticPlan.setTime(10);
                            pneumaticPlan.setType(5);
                        }

                        pneumaticPlanList.add(pneumaticPlan);
                    }

                }

                if (i == 2) {//计划3
                    time = time.plusDays(7);
                    start = df.format(time);
                    end = df.format(localDate);
                    for (int j = 0; j < 5; j++) {
                        PneumaticPlan pneumaticPlan = new PneumaticPlan();
                        pneumaticPlan.setUserId(userId);
                        pneumaticPlan.setStartWeekTime(start + " 00:00:00");
                        pneumaticPlan.setEndWeekTime(end);
                        pneumaticPlan.setPlanType(i + 1);
                        pneumaticPlan.setSort(sort);
                        sort++;
                        pneumaticPlan.setUpdateTime(LocalDateTime.now());
                        if (j == 0) {
                            pneumaticPlan.setName("手套操训练");
                            pneumaticPlan.setTime(30);
                            pneumaticPlan.setType(16);
                        }
                        if (j == 1) {
                            pneumaticPlan.setName("助力训练");
                            pneumaticPlan.setTime(10);
                            pneumaticPlan.setType(11);
                        }
                        if (j == 2) {
                            pneumaticPlan.setName("休息");
                            pneumaticPlan.setTime(40);
                            pneumaticPlan.setType(0);
                        }
                        if (j == 3) {
                            pneumaticPlan.setName("手套操训练");
                            pneumaticPlan.setTime(30);
                            pneumaticPlan.setType(16);
                        }
                        if (j == 4) {
                            pneumaticPlan.setName("镜像训练");
                            pneumaticPlan.setTime(10);
                            pneumaticPlan.setType(5);
                        }

                        pneumaticPlanList.add(pneumaticPlan);
                    }

                }
                if (i == 3) {//计划4
                    time = time.plusDays(7);
                    start = df.format(time);
                    end = df.format(localDate);
                    for (int j = 0; j < 5; j++) {
                        PneumaticPlan pneumaticPlan = new PneumaticPlan();
                        pneumaticPlan.setUserId(userId);
                        pneumaticPlan.setStartWeekTime(start + " 00:00:00");
                        pneumaticPlan.setEndWeekTime(end);
                        pneumaticPlan.setPlanType(i + 1);
                        pneumaticPlan.setSort(sort);
                        sort++;
                        pneumaticPlan.setUpdateTime(LocalDateTime.now());
                        if (j == 0) {
                            pneumaticPlan.setName("手套操训练");
                            pneumaticPlan.setTime(20);
                            pneumaticPlan.setType(16);
                        }
                        if (j == 1) {
                            pneumaticPlan.setName("助力训练");
                            pneumaticPlan.setTime(20);
                            pneumaticPlan.setType(11);
                        }
                        if (j == 2) {
                            pneumaticPlan.setName("休息");
                            pneumaticPlan.setTime(40);
                            pneumaticPlan.setType(0);
                        }
                        if (j == 3) {
                            pneumaticPlan.setName("手套操训练");
                            pneumaticPlan.setTime(20);
                            pneumaticPlan.setType(16);
                        }
                        if (j == 4) {
                            pneumaticPlan.setName("功能训练");
                            pneumaticPlan.setTime(20);
                            pneumaticPlan.setType(7);
                        }

                        pneumaticPlanList.add(pneumaticPlan);
                    }

                }
                if (i == 4) {//计划5
                    time = time.plusDays(7);
                    start = df.format(time);
                    end = df.format(localDate);
                    for (int j = 0; j < 7; j++) {
                        PneumaticPlan pneumaticPlan = new PneumaticPlan();
                        pneumaticPlan.setUserId(userId);
                        pneumaticPlan.setStartWeekTime(start + " 00:00:00");
                        pneumaticPlan.setEndWeekTime(end);
                        pneumaticPlan.setPlanType(i + 1);
                        pneumaticPlan.setSort(sort);
                        sort++;
                        pneumaticPlan.setUpdateTime(LocalDateTime.now());
                        if (j == 0) {
                            pneumaticPlan.setName("手套操训练");
                            pneumaticPlan.setTime(20);
                            pneumaticPlan.setType(16);
                        }
                        if (j == 1) {
                            pneumaticPlan.setName("镜像训练");
                            pneumaticPlan.setTime(10);
                            pneumaticPlan.setType(5);
                        }
                        if (j == 2) {
                            pneumaticPlan.setName("游戏训练");
                            pneumaticPlan.setTime(10);
                            pneumaticPlan.setType(2);
                        }
                        if (j == 3) {
                            pneumaticPlan.setName("休息");
                            pneumaticPlan.setTime(40);
                            pneumaticPlan.setType(0);
                        }
                        if (j == 4) {
                            pneumaticPlan.setName("手套操训练");
                            pneumaticPlan.setTime(20);
                            pneumaticPlan.setType(16);
                        }
                        if (j == 5) {
                            pneumaticPlan.setName("助力训练");
                            pneumaticPlan.setTime(10);
                            pneumaticPlan.setType(11);
                        }
                        if (j == 6) {
                            pneumaticPlan.setName("功能训练");
                            pneumaticPlan.setTime(10);
                            pneumaticPlan.setType(7);
                        }
                        pneumaticPlanList.add(pneumaticPlan);
                    }

                }
                if (i == 5) {//计划6
                    time = time.plusDays(7);
                    start = df.format(time);
                    end = df.format(localDate);
                    for (int j = 0; j < 8; j++) {
                        PneumaticPlan pneumaticPlan = new PneumaticPlan();
                        pneumaticPlan.setUserId(userId);
                        pneumaticPlan.setStartWeekTime(start + " 00:00:00");
                        pneumaticPlan.setEndWeekTime(end);
                        pneumaticPlan.setPlanType(i + 1);
                        pneumaticPlan.setSort(sort);
                        sort++;
                        pneumaticPlan.setUpdateTime(LocalDateTime.now());
                        if (j == 0) {
                            pneumaticPlan.setName("手套操训练");
                            pneumaticPlan.setTime(20);
                            pneumaticPlan.setType(16);
                        }
                        if (j == 1) {
                            pneumaticPlan.setName("镜像训练");
                            pneumaticPlan.setTime(10);
                            pneumaticPlan.setType(5);
                        }
                        if (j == 2) {
                            pneumaticPlan.setName("游戏训练");
                            pneumaticPlan.setTime(10);
                            pneumaticPlan.setType(2);
                        }
                        if (j == 3) {
                            pneumaticPlan.setName("休息");
                            pneumaticPlan.setTime(40);
                            pneumaticPlan.setType(0);
                        }
                        if (j == 4) {
                            pneumaticPlan.setName("手套操训练");
                            pneumaticPlan.setTime(20);
                            pneumaticPlan.setType(16);
                        }
                        if (j == 5) {
                            pneumaticPlan.setName("助力训练");
                            pneumaticPlan.setTime(10);
                            pneumaticPlan.setType(11);
                        }
                        if (j == 6) {
                            pneumaticPlan.setName("抗阻训练");
                            pneumaticPlan.setTime(5);
                            pneumaticPlan.setType(12);
                        }
                        if (j == 7) {
                            pneumaticPlan.setName("功能训练");
                            pneumaticPlan.setTime(5);
                            pneumaticPlan.setType(7);
                        }
                        pneumaticPlanList.add(pneumaticPlan);
                    }

                }
                if (i == 6) {//计划7
                    time = time.plusDays(7);
                    start = df.format(time);
                    end = df.format(localDate);
                    for (int j = 0; j < 7; j++) {
                        PneumaticPlan pneumaticPlan = new PneumaticPlan();
                        pneumaticPlan.setUserId(userId);
                        pneumaticPlan.setStartWeekTime(start + " 00:00:00");
                        pneumaticPlan.setEndWeekTime(end);
                        pneumaticPlan.setPlanType(i + 1);
                        pneumaticPlan.setSort(sort);
                        sort++;
                        pneumaticPlan.setUpdateTime(LocalDateTime.now());
                        if (j == 0) {
                            pneumaticPlan.setName("手套操训练");
                            pneumaticPlan.setTime(20);
                            pneumaticPlan.setType(16);
                        }
                        if (j == 1) {
                            pneumaticPlan.setName("抗阻训练");
                            pneumaticPlan.setTime(10);
                            pneumaticPlan.setType(12);
                        }
                        if (j == 2) {
                            pneumaticPlan.setName("游戏训练");
                            pneumaticPlan.setTime(10);
                            pneumaticPlan.setType(2);
                        }
                        if (j == 3) {
                            pneumaticPlan.setName("休息");
                            pneumaticPlan.setTime(40);
                            pneumaticPlan.setType(0);
                        }
                        if (j == 4) {
                            pneumaticPlan.setName("手套操训练");
                            pneumaticPlan.setTime(20);
                            pneumaticPlan.setType(16);
                        }
                        if (j == 5) {
                            pneumaticPlan.setName("抗阻训练");
                            pneumaticPlan.setTime(10);
                            pneumaticPlan.setType(12);
                        }
                        if (j == 6) {
                            pneumaticPlan.setName("功能训练");
                            pneumaticPlan.setTime(10);
                            pneumaticPlan.setType(7);
                        }
                        pneumaticPlanList.add(pneumaticPlan);
                    }

                }
                if (i == 7) {//计划8
                    time = time.plusDays(7);
                    start = df.format(time);
                    end = df.format(localDate);
                    for (int j = 0; j < 7; j++) {
                        PneumaticPlan pneumaticPlan = new PneumaticPlan();
                        pneumaticPlan.setUserId(userId);
                        pneumaticPlan.setStartWeekTime(start + " 00:00:00");
                        pneumaticPlan.setEndWeekTime(end);
                        pneumaticPlan.setPlanType(i + 1);
                        pneumaticPlan.setSort(sort);
                        sort++;
                        pneumaticPlan.setUpdateTime(LocalDateTime.now());
                        if (j == 0) {
                            pneumaticPlan.setName("手套操训练");
                            pneumaticPlan.setTime(20);
                            pneumaticPlan.setType(16);
                        }
                        if (j == 1) {
                            pneumaticPlan.setName("抗阻训练");
                            pneumaticPlan.setTime(10);
                            pneumaticPlan.setType(12);
                        }
                        if (j == 2) {
                            pneumaticPlan.setName("功能训练");
                            pneumaticPlan.setTime(10);
                            pneumaticPlan.setType(7);
                        }
                        if (j == 3) {
                            pneumaticPlan.setName("休息");
                            pneumaticPlan.setTime(40);
                            pneumaticPlan.setType(0);
                        }
                        if (j == 4) {
                            pneumaticPlan.setName("手套操训练");
                            pneumaticPlan.setTime(10);
                            pneumaticPlan.setType(16);
                        }
                        if (j == 5) {
                            pneumaticPlan.setName("镜像训练");
                            pneumaticPlan.setTime(10);
                            pneumaticPlan.setType(5);
                        }
                        if (j == 6) {
                            pneumaticPlan.setName("游戏训练");
                            pneumaticPlan.setTime(10);
                            pneumaticPlan.setType(2);
                        }
                        if (j == 7) {
                            pneumaticPlan.setName("功能训练");
                            pneumaticPlan.setTime(10);
                            pneumaticPlan.setType(7);
                        }

                        pneumaticPlanList.add(pneumaticPlan);
                    }

                }

            }

            service.saveBatch(pneumaticPlanList);
        }
        list = service.list(new QueryWrapper<PneumaticPlan>().lambda()
                .eq(PneumaticPlan::getUserId, userId));
        Map<String, List<PneumaticPlan>> labelMap = list.stream()
                .collect(Collectors.groupingBy(PneumaticPlan::getStartWeekTime));
        //根据时间排序
        Map<String, List<PneumaticPlan>> map = new TreeMap<String, List<PneumaticPlan>>(
                new Comparator<String>() {
                    @Override
                    public int compare(String obj1, String obj2) {
                        // 升序排序
                        return obj1.compareTo(obj2);
                    }
                });


        for (String key : labelMap.keySet()) {
            map.put(key, labelMap.get(key));
        }
        List<PneumaticPlan> pneumaticPlanList = new ArrayList<>();
        for (String key : map.keySet()) {
            PneumaticPlan pneumaticPlan = new PneumaticPlan();
            pneumaticPlan.setStartWeekTime(key);
            List<PneumaticPlan> pneumaticPlanList2 = map.get(key);
            Collections.sort(pneumaticPlanList2);
            pneumaticPlan.setPneumaticPlanList(pneumaticPlanList2);
            List<PneumaticPlan> pneumaticPlanList1 = map.get(key);
            if (!CollectionUtils.isEmpty(pneumaticPlanList1)) {
                pneumaticPlan.setPlanType(pneumaticPlanList1.get(0).getPlanType());
                pneumaticPlan.setUpdateTime(pneumaticPlanList1.get(0).getUpdateTime());
            }

            pneumaticPlanList.add(pneumaticPlan);
        }
        return RestResponse.ok(pneumaticPlanList);
    }


    @Override
    protected Class<PneumaticPlan> getEntityClass() {
        return PneumaticPlan.class;
    }
}
