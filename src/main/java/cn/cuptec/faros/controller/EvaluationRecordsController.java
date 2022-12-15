package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.EvaluationRecords;
import cn.cuptec.faros.entity.HospitalInfo;
import cn.cuptec.faros.entity.TbPlan;
import cn.cuptec.faros.entity.TbTrainUser;
import cn.cuptec.faros.service.EvaluationRecordsService;
import cn.cuptec.faros.service.PlanUserService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/evaluationRecords")
public class EvaluationRecordsController extends AbstractBaseController<EvaluationRecordsService, EvaluationRecords> {
    @Resource
    private PlanUserService planUserService;

    /**
     * 上传评估记录
     */
    @PostMapping("/addEvaluationRecords")
    public RestResponse addEvaluationRecords(@RequestBody List<EvaluationRecords> list) {
        List<Long> keyIds = list.stream().map(EvaluationRecords::getKeyId)
                .collect(Collectors.toList());
        service.remove(new QueryWrapper<EvaluationRecords>().lambda().in(EvaluationRecords::getKeyId, keyIds));
        Long userId = list.get(0).getUserId();
        planUserService.update(Wrappers.<TbTrainUser>lambdaUpdate()//修改评估记录上传标识
                .eq(TbTrainUser::getUserId, userId+"")
                .set(TbTrainUser::getEvaluationRecordTag, 0)
        );

        service.saveBatch(list);
        return RestResponse.ok();
    }

    /**
     * 查询评估记录
     */
    @GetMapping("/getEvaluationRecords")
    public RestResponse getEvaluationRecords(@RequestParam("userid") Long userid) {
        Page<EvaluationRecords> page = getPage();
        return RestResponse.ok(service.page(page, new QueryWrapper<EvaluationRecords>().lambda().eq(EvaluationRecords::getUserId, userid)));
    }

    /**
     * 查询用户的评估记录
     */
    @GetMapping("/getEvaluationRecordsByXtUserId")
    public RestResponse getEvaluationRecordsByXtUserId() {
        TbTrainUser one = planUserService.getOne(Wrappers.<TbTrainUser>lambdaQuery().eq(TbTrainUser::getXtUserId, SecurityUtils.getUser().getId()));
        if (one == null) {
            return RestResponse.ok();
        }
        List<EvaluationRecords> list = service.list(new QueryWrapper<EvaluationRecords>().lambda().eq(EvaluationRecords::getUserId, one.getUserId()));
        if (!CollectionUtils.isEmpty(list)) {
            for (EvaluationRecords evaluationRecords : list) {
                evaluationRecords.setDate(one.getDate());
                evaluationRecords.setDiagnosis(one.getDiagnosis());
                evaluationRecords.setHospitalName(one.getHospitalName());
                evaluationRecords.setWeight(one.getWeight());
            }
        }
        return RestResponse.ok(list);
    }
    @GetMapping("/getEvaluationRecordsUserId")
    public RestResponse getEvaluationRecordsUserId(@RequestParam("userId") Long userId) {
        TbTrainUser one = planUserService.getOne(Wrappers.<TbTrainUser>lambdaQuery().eq(TbTrainUser::getUserId, userId));
        if (one == null) {
            return RestResponse.ok();
        }
        List<EvaluationRecords> list = service.list(new QueryWrapper<EvaluationRecords>().lambda().eq(EvaluationRecords::getUserId, one.getUserId()));
        if (!CollectionUtils.isEmpty(list)) {
            for (EvaluationRecords evaluationRecords : list) {
                evaluationRecords.setDate(one.getDate());
                evaluationRecords.setDiagnosis(one.getDiagnosis());
                evaluationRecords.setHospitalName(one.getHospitalName());
                evaluationRecords.setWeight(one.getWeight());
            }
        }
        return RestResponse.ok(list);
    }
    /**
     * 根据身份证查询评估记录
     *
     * @param phone
     * @param idCard
     * @return
     */
    @GetMapping("listGroupByPhoneAndIdCard")
    public RestResponse listGroupByPhoneAndIdCard(@RequestParam(value = "phone", required = false) String phone, @RequestParam(value = "idCard", required = false) String idCard,@RequestParam(value = "userId", required = false) String userId) {

        LambdaQueryWrapper<TbTrainUser> eq = new QueryWrapper<TbTrainUser>().lambda();
        if(!StringUtils.isEmpty(idCard)){
            eq.eq(TbTrainUser::getIdCard, idCard);
        }else if(!StringUtils.isEmpty(userId)){
            eq.eq(TbTrainUser::getXtUserId, userId);
        }


        List<TbTrainUser> list = planUserService.list(eq);
        if (CollectionUtils.isEmpty(list)) {
            return RestResponse.ok();
        }
        TbTrainUser tbTrainUser = list.get(0);
        List<EvaluationRecords> data = service.list(new QueryWrapper<EvaluationRecords>().lambda().eq(EvaluationRecords::getUserId, tbTrainUser.getUserId()));
        if (!CollectionUtils.isEmpty(data)) {
            for (EvaluationRecords evaluationRecords : data) {
                evaluationRecords.setDate(tbTrainUser.getDate());
                evaluationRecords.setDiagnosis(tbTrainUser.getDiagnosis());
                evaluationRecords.setHospitalName(tbTrainUser.getHospitalName());
                evaluationRecords.setWeight(tbTrainUser.getWeight());
            }
        }


        return RestResponse.ok(data);
    }

    @Override
    protected Class<EvaluationRecords> getEntityClass() {
        return EvaluationRecords.class;
    }
}
