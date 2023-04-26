package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.service.PlanUserService;
import cn.cuptec.faros.service.PlanUserTrainRecordService;
import cn.cuptec.faros.service.QuestionnaireGroupService;
import cn.cuptec.faros.service.QuestionnaireService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 问卷答案
 */
@RestController
@RequestMapping("/questionnaire")
public class QuestionnaireController extends AbstractBaseController<QuestionnaireService, Questionnaire> {
    @Resource
    private QuestionnaireGroupService questionnaireGroupService;
    @Resource
    private PlanUserService planUserService;
    /**
     * 添加
     */
    @PostMapping("/add")
    public RestResponse add(@RequestBody List<Questionnaire> questionnaires) {
        QuestionnaireGroup group = new QuestionnaireGroup();
        group.setUserId(questionnaires.get(0).getUserId());
        questionnaireGroupService.save(group);
        int score = 0;
        int sort = 1;
        for (int i = 0; i < questionnaires.size(); i++) {
            Questionnaire questionnaire = questionnaires.get(i);
            questionnaire.setCreateTime(LocalDateTime.now());
            questionnaire.setSort(sort);
            questionnaire.setGroupId(group.getId());
            sort++;
            //计算评分
            if (i == 0) {
                if (questionnaire.getAnswer().equals(1)) {
                    score = score + 5;
                } else {
                    score = score + 10;
                }
            }
            if (i == 1) {
                if (questionnaire.getAnswer().equals(1)) {
                    score = score + 5;
                } else {
                    score = score + 10;
                }
            }
            if (i == 2) {
                if (questionnaire.getAnswer().equals(1)) {
                    score = score + 5;
                }
            }
            if (i == 3) {
                if (questionnaire.getAnswer().equals(1)) {
                    score = score + 5;
                } else {
                    score = score + 10;
                }
            }
            if (i == 4) {
                if (questionnaire.getAnswer().equals(1)) {
                    score = score + 5;
                } else {
                    score = score + 10;
                }
            }
            if (i == 5) {
                if (questionnaire.getAnswer().equals(1)) {
                    score = score + 5;
                } else if (questionnaire.getAnswer().equals(2)) {
                    score = score + 10;
                } else {
                    score = score + 15;
                }
            }
            if (i == 6) {
                if (questionnaire.getAnswer().equals(1)) {
                    score = score + 5;
                } else if (questionnaire.getAnswer().equals(2)) {
                    score = score + 10;
                } else {
                    score = score + 15;
                }
            }
            if (i == 7) {
                if (questionnaire.getAnswer().equals(1)) {
                    score = score + 5;
                } else {
                    score = score + 10;
                }
            }
            if (i == 8) {
                if (questionnaire.getAnswer().equals(1)) {
                    score = score + 5;
                } else {
                    score = score + 10;
                }
            }
            if (i == 9) {
                if (questionnaire.getAnswer().equals(1)) {
                    score = score + 5;
                }
            }
        }
        group.setScore(score);
        group.setCreateTime(LocalDateTime.now());
        questionnaireGroupService.updateById(group);
        service.saveBatch(questionnaires);
        return RestResponse.ok();
    }

    /**
     * 查询试卷
     */
    @GetMapping("/getByIdCard")
    public RestResponse getByUserId(@RequestParam(value = "idCard",required = false) String idCard,@RequestParam(value = "userId",required = false) String userId) {
        LambdaQueryWrapper<TbTrainUser> queryWrapper = new LambdaQueryWrapper<>();

        if (!StringUtils.isEmpty(idCard)) {
            queryWrapper.eq(TbTrainUser::getIdCard, idCard);
        }
        if (!StringUtils.isEmpty(userId)) {
            queryWrapper.eq(TbTrainUser::getXtUserId, userId);
        }
        List<TbTrainUser> list1 = planUserService.list(queryWrapper);
        if(CollectionUtils.isEmpty(list1)){
            return RestResponse.ok(new ArrayList<>());
        }
        List<QuestionnaireGroup> list = questionnaireGroupService.list(new QueryWrapper<QuestionnaireGroup>().lambda().eq(QuestionnaireGroup::getUserId, list1.get(0).getUserId()));
        return RestResponse.ok(list);
    }

    /**
     * 查询试卷详情
     */
    @GetMapping("/getByUserIdGroupDetail")
    public RestResponse getByUserIdGroupDetail(@RequestParam("groupId") Integer groupId) {
        List<Questionnaire> list = service.list(new QueryWrapper<Questionnaire>().lambda().eq(Questionnaire::getGroupId, groupId).orderByAsc(Questionnaire::getSort));
        return RestResponse.ok(list);
    }

    @Override
    protected Class<Questionnaire> getEntityClass() {
        return Questionnaire.class;
    }

}
