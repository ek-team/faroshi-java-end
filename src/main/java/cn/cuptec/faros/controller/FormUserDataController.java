package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.dto.FormUserDataParam;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.service.*;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户填写的表单数据
 */
@RestController
@RequestMapping("/formUserData")
public class FormUserDataController extends AbstractBaseController<FormUserDataService, FormUserData> {
    @Resource
    private FormService formService;
    @Resource
    private FollowUpPlanNoticeService followUpPlanNoticeService;
    @Resource
    private FormSettingService formSettingService;
    @Resource
    private FormOptionsService formOptionsService;
    @Resource
    private UpcomingService upcomingService;

    /**
     * 添加表单内容
     */
    @PostMapping("/saveData")
    public RestResponse saveData(@RequestBody FormUserDataParam param) {
        String groupId = IdUtil.getSnowflake(0, 0).nextIdStr();
        List<FormUserData> formManagementData = param.getFormManagementDatas();
        Integer formId = formManagementData.get(0).getFormId();
        Form byId = formService.getById(formId);

        service.remove(new QueryWrapper<FormUserData>().lambda()
                .eq(FormUserData::getFormId, formManagementData.get(0).getFormId())
                .eq(FormUserData::getDoctorId, byId.getCreateUserId())
                .eq(FormUserData::getUserId, SecurityUtils.getUser().getId()));
        List<Integer> formSettingId = formManagementData.stream().map(FormUserData::getFormSettingId)
                .collect(Collectors.toList());
        List<FormSetting> formSettings = (List<FormSetting>) formSettingService.listByIds(formSettingId);
        List<FormOptions> formOptions = formOptionsService.list(new QueryWrapper<FormOptions>().lambda()
                .in(FormOptions::getFormSettingId, formSettingId));
        if (!org.springframework.util.CollectionUtils.isEmpty(formOptions)) {
            Map<Integer, List<FormOptions>> map = formOptions.stream()
                    .collect(Collectors.groupingBy(FormOptions::getFormSettingId));
            for (FormSetting formSetting : formSettings) {
                formSetting.setFormOptionsList(map.get(formSetting.getId()));
            }
        }
        Map<Integer, FormSetting> formSettingMap = formSettings.stream()
                .collect(Collectors.toMap(FormSetting::getId, t -> t));
        for (FormUserData formUserData : formManagementData) {
            formUserData.setGroupId(groupId);
            formUserData.setUserId(SecurityUtils.getUser().getId());
            formUserData.setCreateTime(LocalDateTime.now());
            formUserData.setDoctorId(byId.getCreateUserId());
            //计算分数
            Object answer = formUserData.getAnswer();
            String type = formUserData.getType();
            Double scope = 0.0;
            FormSetting formSetting = formSettingMap.get(formUserData.getFormSettingId());
            List<FormOptions> formOptionsList = formSetting.getFormOptionsList();
            if (type.equals("1")) {//1-输入框
                for (FormOptions formOption : formOptionsList) {
                    if (answer.equals(formOption.getText())) {
                        scope = scope + formOption.getScore();
                    }
                }

            } else if (type.equals("2")) {// 2-单选框
                for (FormOptions formOption : formOptionsList) {
                    if (answer.equals(formOption.getId())) {
                        scope = scope + formOption.getScore();
                    }
                }
            } else if (type.equals("3")) {//3-多行入框
                for (FormOptions formOption : formOptionsList) {
                    if (answer.equals(formOption.getText())) {
                        scope = scope + formOption.getScore();
                    }
                }
            } else if (type.equals("4")) {//4文本

            } else if (type.equals("5")) {//5图片

            } else if (type.equals("6")) {// 6 -多选框
                for (FormOptions formOption : formOptionsList) {
                    if (answer.equals(formOption.getId())) {
                        scope = scope + formOption.getScore();
                    }
                }
            }
            formUserData.setScope(scope);
        }
        service.saveBatch(formManagementData);
        //更改通知的状态
        Integer str = formManagementData.get(0).getStr();
        FollowUpPlanNotice followUpPlanNotice = new FollowUpPlanNotice();
        followUpPlanNotice.setId(str);
        followUpPlanNotice.setForm(1);
        followUpPlanNoticeService.updateById(followUpPlanNotice);

        //添加待办事项
        Upcoming upcoming=new Upcoming();
        upcoming.setContent("用户填写表单成功");
        upcoming.setTitle("表单");
        upcoming.setUserId(SecurityUtils.getUser().getId());
        upcoming.setDoctorId(formManagementData.get(0).getDoctorId());
        upcoming.setCreateTime(LocalDateTime.now());
        upcoming.setType("2");
        upcomingService.save(upcoming);
        return RestResponse.ok();
    }


    /**
     * 查询 用户填写的表单数据 根据订单id
     */
    @GetMapping("/getDataByOrderId")
    public RestResponse getDataByOrderId(@RequestParam("orderId") String orderId) {

        //题目数据
        List<FormUserData> list = service.list(new QueryWrapper<FormUserData>().lambda().eq(FormUserData::getOrderId, orderId));
        if (CollectionUtils.isEmpty(list)) {
            return RestResponse.ok();
        }
        List<Integer> formIds = list.stream().map(FormUserData::getFormId)
                .collect(Collectors.toList());
        Map<Integer, List<FormUserData>> map = list.stream()
                .collect(Collectors.groupingBy(FormUserData::getFormId));

        List<Form> formList = formService.getByIds(formIds);
        for (Form form : formList) {
            List<FormUserData> formUserDataList = map.get(form.getId());
            List<FormUserData> collect = formUserDataList.stream()
                    .sorted(Comparator.comparing(FormUserData::getFormSettingId)).collect(Collectors.toList());
            form.setFormUserDataList(collect);

        }
        return RestResponse.ok(formList);
    }

    /**
     * 医生端查询 用户填写的表单
     */
    @GetMapping("/getDataByFormId")
    public RestResponse getDataByFormId(@RequestParam(value = "userId", required = false) Integer userId) {

        //题目数据
        List<FormUserData> list = service.list(new QueryWrapper<FormUserData>().lambda()
                .eq(FormUserData::getUserId, userId)
                .eq(FormUserData::getDoctorId, SecurityUtils.getUser().getId()));
        if (CollectionUtils.isEmpty(list)) {
            return RestResponse.ok();
        }
        List<Integer> formIds = list.stream().map(FormUserData::getFormId)
                .collect(Collectors.toList());
        List<Form> formList = (List<Form>) formService.listByIds(formIds);
        return RestResponse.ok(formList);
    }

    /**
     * 医生端查询 用户填写的表单详情
     */
    @GetMapping("/getDataDetailByFormId")
    public RestResponse getDataDetailByFormId(@RequestParam(value = "formId", required = false) Integer formId, @RequestParam(value = "userId", required = false) Integer userId,
                                              @RequestParam(value = "doctorId", required = false) Integer doctorId) {

        //题目数据
        List<FormUserData> list = service.list(new QueryWrapper<FormUserData>().lambda()
                .eq(FormUserData::getUserId, userId)
                .eq(FormUserData::getFormId, formId)
                .eq(FormUserData::getDoctorId, SecurityUtils.getUser().getId()));
        if (CollectionUtils.isEmpty(list)) {
            return RestResponse.ok();
        }

        for (FormUserData formUserData : list) {
            if (!StringUtils.isEmpty(formUserData.getAnswer())) {
                Object answer = formUserData.getAnswer();
                String s = answer.toString();
                if (s.indexOf("[") >= 0) {
                    String replace = s.replace("[", "");
                    String replace1 = replace.replace("]", "");
                    String replace2 = replace1.replace("\"", "");

                    String[] split = replace2.split(",");
                    List<String> strings = Arrays.asList(split);
                    if (formUserData.getType().equals("6")) {
                        List<Integer> ans = new ArrayList<>();
                        for (String str : strings) {
                            ans.add(Integer.parseInt(str));
                        }
                        formUserData.setAnswer(ans);
                    } else {

                        formUserData.setAnswer(strings);
                    }

                }

            }

        }

        List<Integer> formIds = list.stream().map(FormUserData::getFormId)
                .collect(Collectors.toList());
        List<Form> formList = formService.getByIds(formIds);
        Map<Integer, List<FormUserData>> map = list.stream()
                .collect(Collectors.groupingBy(FormUserData::getFormId));
        for (Form form : formList) {
            List<FormUserData> formUserDataList = map.get(form.getId());
            Double scope = 0.0;
            for (FormUserData formUserData : formUserDataList) {
                if (formUserData.getScope() != null) {
                    scope = formUserData.getScope() + scope;
                }
            }
            List<FormUserData> collect = formUserDataList.stream()
                    .sorted(Comparator.comparing(FormUserData::getFormSettingId)).collect(Collectors.toList());
            form.setFormUserDataList(collect);
            form.setScope(scope);
        }
        return RestResponse.ok(formList.get(0));
    }

    /**
     * PC查询 用户填写的表单详情根据分组id
     */
    @GetMapping("/getDataDetailByGroupId")
    public RestResponse getDataDetailByGroupId(@RequestParam(value = "groupId", required = false) String groupId) {

        //题目数据
        List<FormUserData> list = service.list(new QueryWrapper<FormUserData>().lambda()
                .eq(FormUserData::getGroupId, groupId));
        if (CollectionUtils.isEmpty(list)) {
            return RestResponse.ok();
        }

        for (FormUserData formUserData : list) {
            if (!StringUtils.isEmpty(formUserData.getAnswer())) {
                Object answer = formUserData.getAnswer();
                String s = answer.toString();
                if (s.indexOf("[") >= 0) {
                    String replace = s.replace("[", "");
                    String replace1 = replace.replace("]", "");
                    String replace2 = replace1.replace("\"", "");

                    String[] split = replace2.split(",");
                    List<String> strings = Arrays.asList(split);
                    if (formUserData.getType().equals("6")) {
                        List<Integer> ans = new ArrayList<>();
                        for (String str : strings) {
                            ans.add(Integer.parseInt(str));
                        }
                        formUserData.setAnswer(ans);
                    } else {

                        formUserData.setAnswer(strings);
                    }

                }

            }

        }

        List<Integer> formIds = list.stream().map(FormUserData::getFormId)
                .collect(Collectors.toList());
        List<Form> formList = formService.getByIds(formIds);
        Map<Integer, List<FormUserData>> map = list.stream()
                .collect(Collectors.groupingBy(FormUserData::getFormId));
        for (Form form : formList) {
            List<FormUserData> formUserDataList = map.get(form.getId());
            Double scope = 0.0;
            for (FormUserData formUserData : formUserDataList) {
                if (formUserData.getScope() != null) {
                    scope = formUserData.getScope() + scope;
                }
            }
            List<FormUserData> collect = formUserDataList.stream()
                    .sorted(Comparator.comparing(FormUserData::getFormSettingId)).collect(Collectors.toList());
            form.setFormUserDataList(collect);
            form.setScope(scope);
        }
        return RestResponse.ok(formList.get(0));
    }

    @Override
    protected Class<FormUserData> getEntityClass() {
        return FormUserData.class;
    }

}
