package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.mapper.PlanMapper;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.xml.crypto.Data;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PlanService extends ServiceImpl<PlanMapper, TbPlan> {

    @Resource
    private SubPlanService subPlanService;

    @Resource
    private PlanUserService planUserService;

    @Transactional(rollbackFor = Exception.class)
    public void saveList(List<TbPlan> planList) {


        if (CollUtil.isEmpty(planList)) return;
        TbPlan tbPlan = planList.get(0);
        Long userId = tbPlan.getUserId();
        //删除改用户之前的计划
        this.remove(new QueryWrapper<TbPlan>().lambda().eq(TbPlan::getUserId, userId));
        this.saveBatch(planList);
        for (TbPlan plan : planList) {
            if (CollUtil.isNotEmpty(plan.getSubPlanEntityList())) {
                Page<TbSubPlan> page = new Page<>();
                page.setSize(1);
                page.setCurrent(1);
                QueryWrapper queryWrapper = new QueryWrapper<TbSubPlan>();
                queryWrapper.eq("user_id", userId);
                queryWrapper.orderByDesc("version");
                IPage page1 = subPlanService.page(page, queryWrapper);
                if (CollectionUtils.isEmpty(page1.getRecords())) {
                    for (TbSubPlan tbSubPlan : plan.getSubPlanEntityList()) {
                        tbSubPlan.setInitStart(1);
                    }

                } else {
                    List<TbSubPlan> records = page1.getRecords();
                    TbSubPlan tbSubPlan1 = records.get(0);
                    for (TbSubPlan tbSubPlan : plan.getSubPlanEntityList()) {
                        tbSubPlan.setInitStart(2);
                        tbSubPlan.setVersion(tbSubPlan1.getVersion() + 1);
                    }
                }
                subPlanService.saveBatch(plan.getSubPlanEntityList());
            }
        }

    }

    public List<TbPlan> getListByUid(String uid) {

        List<TbPlan> list = this.list(Wrappers.<TbPlan>lambdaQuery().eq(TbPlan::getUserId, uid).orderByAsc(TbPlan::getStartDate));
        if (CollUtil.isNotEmpty(list)) {
            //查询最新版本的计划
            Page<TbSubPlan> page = new Page<>();
            page.setSize(1);
            page.setCurrent(1);
            QueryWrapper queryWrapper = new QueryWrapper<TbSubPlan>();
            queryWrapper.eq("user_id", uid);
            queryWrapper.orderByDesc("version");
            IPage page1 = subPlanService.page(page, queryWrapper);
            List<TbSubPlan> subPlans = page1.getRecords();
            if (!CollectionUtils.isEmpty(subPlans)) {
                Integer version = subPlans.get(0).getVersion();
                subPlans = subPlanService.list(new QueryWrapper<TbSubPlan>().lambda().eq(TbSubPlan::getVersion, version).eq(TbSubPlan::getUserId, uid));
            }
            if (!CollectionUtils.isEmpty(subPlans)) {
//                Integer key = 1;
//                for (TbSubPlan tbSubPlan : subPlans) {
//                    if (key < tbSubPlan.getVersion()) {
//                        key = tbSubPlan.getVersion();
//                    }
//                }
//                Map<Integer, List<TbSubPlan>> map = subPlans.stream()
//                        .collect(Collectors.groupingBy(TbSubPlan::getVersion));
                Collections.sort(subPlans);
                list.get(0).setSubPlanEntityList(subPlans);
            }

        }


        return list;
    }

    public List<TbPlan> getStartListByUid(String uid) {

        List<TbPlan> list = this.list(Wrappers.<TbPlan>lambdaQuery().eq(TbPlan::getUserId, uid).orderByAsc(TbPlan::getStartDate));
        if (CollUtil.isNotEmpty(list)) {
            //查询最新版本的计划
            Page<TbSubPlan> page = new Page<>();
            page.setSize(1);
            page.setCurrent(1);
            QueryWrapper queryWrapper = new QueryWrapper<TbSubPlan>();
            queryWrapper.eq("user_id", uid);
            queryWrapper.orderByAsc("version");
            IPage page1 = subPlanService.page(page, queryWrapper);
            List<TbSubPlan> subPlans = page1.getRecords();
            if (!CollectionUtils.isEmpty(subPlans)) {
                Integer version = subPlans.get(0).getVersion();
                subPlans = subPlanService.list(new QueryWrapper<TbSubPlan>().lambda().eq(TbSubPlan::getVersion, version).eq(TbSubPlan::getUserId, uid));
            }
            if (!CollectionUtils.isEmpty(subPlans)) {
//                Integer key = 1;
//                for (TbSubPlan tbSubPlan : subPlans) {
//                    if (key < tbSubPlan.getVersion()) {
//                        key = tbSubPlan.getVersion();
//                    }
//                }
//                Map<Integer, List<TbSubPlan>> map = subPlans.stream()
//                        .collect(Collectors.groupingBy(TbSubPlan::getVersion));
                Collections.sort(subPlans);
                list.get(0).setSubPlanEntityList(subPlans);
            }

        }


        return list;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateList(List<TbPlan> tbPlanList) {
        if (CollUtil.isNotEmpty(tbPlanList)) {
            Date date = new Date();
            tbPlanList.forEach(plan -> {
                LambdaUpdateWrapper<TbPlan> wrapper = new UpdateWrapper<TbPlan>().lambda()
                        .set(TbPlan::getTimeOfDay, plan.getTimeOfDay())
                        .set(TbPlan::getCountOfTime, plan.getCountOfTime())
                        .set(TbPlan::getLoad, plan.getLoad())
                        .set(TbPlan::getUpdateDate, date)
                        .eq(TbPlan::getId, plan.getId());
                this.update(wrapper);
            });
        }

    }

    public List<TbPlan> listGroupByXtUserId(Integer xtUserId) {
        TbTrainUser infoByUXtUserId = planUserService.getOne(Wrappers.<TbTrainUser>lambdaQuery().eq(TbTrainUser::getId, xtUserId));//getInfoByUXtUserId(xtUserId);
        if (infoByUXtUserId == null) return CollUtil.toList();
        List<TbPlan> list = this.list(Wrappers.<TbPlan>lambdaQuery().eq(TbPlan::getUserId, infoByUXtUserId.getUserId()).orderByAsc(TbPlan::getStartDate));

        return list;
    }

    public List<TbPlan> listGroupByXtUserIdData(Integer xtUserId) {
        TbTrainUser infoByUXtUserId = planUserService.getOne(Wrappers.<TbTrainUser>lambdaQuery().eq(TbTrainUser::getXtUserId, xtUserId));//getInfoByUXtUserId(xtUserId);
        if (infoByUXtUserId == null) return CollUtil.toList();
        return this.list(Wrappers.<TbPlan>lambdaQuery().eq(TbPlan::getUserId, infoByUXtUserId.getUserId()).orderByAsc(TbPlan::getStartDate));
    }

    public List<TbPlan> listGroupByPhone(String phone, String idCard,String userId) {
        TbTrainUser infoByUXtUserId = planUserService.getInfoByPhoneAndIdCard(phone, idCard,userId);
        if (infoByUXtUserId == null) return CollUtil.toList();


        List<TbPlan> list = this.list(Wrappers.<TbPlan>lambdaQuery().eq(TbPlan::getUserId, infoByUXtUserId.getUserId()).orderByAsc(TbPlan::getStartDate));
        if (CollUtil.isNotEmpty(list)) {
            //查询最新版本的计划
            Page<TbSubPlan> page = new Page<>();
            page.setSize(1);
            page.setCurrent(1);
            QueryWrapper queryWrapper = new QueryWrapper<TbSubPlan>();
            queryWrapper.eq("user_id", infoByUXtUserId.getUserId());
            queryWrapper.orderByDesc("version");
            IPage page1 = subPlanService.page(page, queryWrapper);
            List<TbSubPlan> subPlans = page1.getRecords();
            if (!CollectionUtils.isEmpty(subPlans)) {
                Integer version = subPlans.get(0).getVersion();
                subPlans = subPlanService.list(new QueryWrapper<TbSubPlan>().lambda().eq(TbSubPlan::getVersion, version).eq(TbSubPlan::getUserId, infoByUXtUserId.getUserId()));
            }
            if (!CollectionUtils.isEmpty(subPlans)) {

                list.get(0).setSubPlanEntityList(subPlans);
            }

        }


        return list;
    }
}
