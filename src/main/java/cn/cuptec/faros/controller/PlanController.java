package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.service.DoctorTeamService;
import cn.cuptec.faros.service.EvaluationRecordsService;
import cn.cuptec.faros.service.PlanService;
import cn.cuptec.faros.service.PlanUserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Update;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 康复计划
 */
@RestController
@RequestMapping("/plan")
public class PlanController extends AbstractBaseController<PlanService, TbPlan> {
    @Resource
    private PlanUserService planUserService;
    @Resource
    private EvaluationRecordsService evaluationRecordsService;
    @Resource
    private DoctorTeamService doctorTeamService;

    @PostMapping("/save")
    public RestResponse addPlan(@RequestBody List<TbPlan> planList) {
        if (CollectionUtils.isEmpty(planList)) {
            return RestResponse.ok();
        }
        service.saveList(planList);
        Long userId = planList.get(0).getUserId();

        TbTrainUser one = planUserService.getOne(Wrappers.<TbTrainUser>lambdaQuery().eq(TbTrainUser::getUserId, userId));
        if (one != null) {
            if (one.getDoctorTeamId() == null || one.getDoctorTeamId().equals(0)) {
                one.setPlanCheckStatus(2);
                planUserService.updateById(one);
            }
            DoctorTeam doctorTeam = doctorTeamService.getOne(new QueryWrapper<DoctorTeam>().lambda().eq(DoctorTeam::getId, one.getDoctorTeamId())
                    .eq(DoctorTeam::getPlanCheckStatus, 1));
            if (doctorTeam == null) {

                one.setPlanCheckStatus(2);
                planUserService.updateById(one);
                return RestResponse.ok();

            }

        }

        return RestResponse.ok();
    }


    /**
     * 给设备同步使用，多个Plan,只有第一个填充subplan
     *
     * @param uid
     * @return
     */
    @GetMapping("listByUid/{uid}")
    public ListByUidPlanResult listByUid(@PathVariable String uid) {
        List<TbPlan> list = service.getListByUid(uid);
        ListByUidPlanResult result = new ListByUidPlanResult();
        result.setData(list);
        result.setOriginalData(service.getStartListByUid(uid));
        result.setCode(0);
        //查询开始计划
        return result;
    }

    @GetMapping("listByxtUserId/{uid}")
    public RestResponse listByXtUserId(@PathVariable long uid) {
        TbTrainUser one = planUserService.getOne(Wrappers.<TbTrainUser>lambdaQuery().eq(TbTrainUser::getXtUserId, uid));
        if (one == null) {
            return RestResponse.ok();
        }
        List<TbPlan> list = service.getListByUid(one.getUserId());
        return RestResponse.ok(list);
    }

    /**
     * 给H5医生使用，
     *
     * @return
     */
    @GetMapping("listGroupByXtUserId")
    public RestResponse listGroupByXtUserId(@RequestParam("xtUserId") Integer xtUserId) {

        return RestResponse.ok(service.listGroupByXtUserId(xtUserId));
    }

    @GetMapping("listGroupByXtUserIdData")
    public RestResponse listGroupByXtUserIdData(@RequestParam("xtUserId") Integer xtUserId) {

        return RestResponse.ok(service.listGroupByXtUserIdData(xtUserId));
    }

    @GetMapping("listGroupByPhoneAndIdCard")
    public RestResponse listGroupByPhoneAndIdCard(@RequestParam(value = "userId", required = false) String userId, @RequestParam(value = "phone", required = false) String phone, @RequestParam(value = "idCard", required = false) String idCard) {

        return RestResponse.ok(service.listGroupByPhone(phone, idCard, userId));
    }

    @PutMapping("updateList")
    public RestResponse updateList(@RequestBody List<TbPlan> tbPlanList) {
        service.updateList(tbPlanList);
        Long userId = tbPlanList.get(0).getUserId();

        TbTrainUser one = planUserService.getOne(Wrappers.<TbTrainUser>lambdaQuery().eq(TbTrainUser::getUserId, userId));
        if (one != null) {
            if (one.getDoctorTeamId() == null || one.getDoctorTeamId().equals(0)) {
                one.setPlanCheckStatus(2);
                planUserService.updateById(one);
            }
            DoctorTeam doctorTeam = doctorTeamService.getOne(new QueryWrapper<DoctorTeam>().lambda().eq(DoctorTeam::getId, one.getDoctorTeamId())
                    .eq(DoctorTeam::getPlanCheckStatus, 1));
            if (doctorTeam == null) {

                one.setPlanCheckStatus(2);
                planUserService.updateById(one);
                return RestResponse.ok();

            }

        }
        return RestResponse.ok();
    }

    /**
     * 清除康复计划标识
     *
     * @return
     */
    @GetMapping("cleanTag")
    public RestResponse cleanTag(@RequestParam("userId") String userId) {
        planUserService.update(Wrappers.<TbTrainUser>lambdaUpdate()//修改评估记录上传标识
                .eq(TbTrainUser::getUserId, userId + "")
                .set(TbTrainUser::getEvaluationRecordTag, 0));

        return RestResponse.ok();
    }

    @Override
    protected Class<TbPlan> getEntityClass() {
        return TbPlan.class;
    }
}
