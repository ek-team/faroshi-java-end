package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.service.*;
import cn.cuptec.faros.util.ExcelUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.common.collect.Lists;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 表单管理
 */
@RestController
@RequestMapping("/form")
public class FormController extends AbstractBaseController<FormService, Form> {
    @Resource
    private FormOptionsService formOptionsService;
    @Resource
    private FormSettingService formSettingService;
    @Resource
    private UserService userService;
    @Resource
    private FormUserDataService formUserDataService;
    @Resource
    private ChatMsgService chatMsgService;
    @Resource
    private FollowUpPlanNoticeService followUpPlanNoticeService;

    /**
     * 添加
     *
     * @param form
     * @return
     */
    @PostMapping("/save")
    public RestResponse save(@RequestBody Form form) {
        form.setCreateTime(LocalDateTime.now());
        User byId = userService.getById(SecurityUtils.getUser().getId());
        form.setDeptId(byId.getDeptId());
        form.setCreateUserId(byId.getId());
        service.save(form);
        List<FormSetting> formSettings = form.getFormSettings();
        for (FormSetting formSetting : formSettings) {
            formSetting.setFormId(form.getId());
        }
        formSettingService.saveBatch(formSettings);
        List<FormOptions> addFormOptionsList = new ArrayList<>();
        for (FormSetting formSetting : formSettings) {
            List<FormOptions> formOptionsList = formSetting.getFormOptionsList();
            if (!CollectionUtils.isEmpty(formOptionsList)) {
                for (FormOptions formOptions : formOptionsList) {
                    formOptions.setFormId(form.getId());
                    formOptions.setFormSettingId(formSetting.getId());
                }
                addFormOptionsList.addAll(formOptionsList);
            }


        }
        if (!CollectionUtils.isEmpty(addFormOptionsList)) {
            formOptionsService.saveBatch(addFormOptionsList);
        }
        return RestResponse.ok();
    }

    /**
     * 作废
     *
     * @param id
     * @return
     */
    @GetMapping("/deleteById")
    public RestResponse deleteById(@RequestParam("id") Integer id) {
        Form form = new Form();
        form.setId(id);
        form.setStatus(1);
        service.updateById(form);

        return RestResponse.ok();
    }

    /**
     * 修改
     *
     * @param form
     * @return
     */
    @PostMapping("/updateById")
    public RestResponse updateById(@RequestBody Form form) {

        service.updateById(form);
        formSettingService.remove(new QueryWrapper<FormSetting>().lambda()
                .eq(FormSetting::getFormId, form.getId()));
        formOptionsService.remove(new QueryWrapper<FormOptions>().lambda()
                .eq(FormOptions::getFormId, form.getId()));

        List<FormSetting> formSettings = form.getFormSettings();
        for (FormSetting formSetting : formSettings) {
            formSetting.setFormId(form.getId());
        }
        formSettingService.saveBatch(formSettings);
        List<FormOptions> addFormOptionsList = new ArrayList<>();
        for (FormSetting formSetting : formSettings) {
            List<FormOptions> formOptionsList = formSetting.getFormOptionsList();
            for (FormOptions formOptions : formOptionsList) {
                formOptions.setFormId(form.getId());
                formOptions.setFormSettingId(formSetting.getId());
            }
            addFormOptionsList.addAll(formOptionsList);
        }
        if (!CollectionUtils.isEmpty(addFormOptionsList)) {
            formOptionsService.saveBatch(addFormOptionsList);
        }
        return RestResponse.ok();
    }

    /**
     * 分页查询
     *
     * @return
     */
    @GetMapping("/pageScoped")
    public RestResponse pageScoped() {
        Page<Form> page = getPage();
        QueryWrapper queryWrapper = getQueryWrapper(getEntityClass());
        IPage<Form> formIPage = service.pageScoped(page, queryWrapper);
        return RestResponse.ok(formIPage);
    }

    /**
     * 查询某一表单用户填写的数据
     */
    @GetMapping("/pageFormUserData")
    public RestResponse pageFormUserData(@RequestParam("formId") Integer formId) {
        List<FormUserData> formUserDataList = formUserDataService.list(new QueryWrapper<FormUserData>().lambda()
                .eq(FormUserData::getFormId, formId)
        );
        if (!CollectionUtils.isEmpty(formUserDataList)) {
            List<Integer> userIds = new ArrayList<>();
            for (FormUserData formUserData : formUserDataList) {
                userIds.add(formUserData.getUserId());
                userIds.add(formUserData.getDoctorId());
            }

            List<User> users = (List<User>) userService.listByIds(userIds);
            Map<Integer, User> userMap = users.stream()
                    .collect(Collectors.toMap(User::getId, t -> t));
            Map<String, List<FormUserData>> formUserDataMap = formUserDataList.stream()
                    .collect(Collectors.groupingBy(FormUserData::getGroupId));
            List<Form> formDatas = new ArrayList<>();
            for (List<FormUserData> formUserDatas : formUserDataMap.values()) {
                Form form = new Form();
                form.setGroupId(formUserDatas.get(0).getGroupId());
                form.setCreateTime(formUserDatas.get(0).getCreateTime());
                Double scope = 0.0;
                for (FormUserData formUserData : formUserDatas) {
                    if (formUserData.getScope() != null) {
                        scope = formUserData.getScope() + scope;
                    }
                    form.setUserName(userMap.get(formUserData.getUserId()).getPatientName());
                    form.setDoctorName(userMap.get(formUserData.getDoctorId()).getNickname());
                }
                form.setId(formId);
                form.setScope(scope);

                formDatas.add(form);
            }
            return RestResponse.ok(formDatas);
        }
        return RestResponse.ok();
    }

    /**
     * 导出某一表单所有用户填写题目数据
     */
    @GetMapping("/exportFormUserData")
    public RestResponse exportFormUserData(HttpServletResponse response, @RequestParam("formId") Integer formId) {
        List<FormUserData> formUserDataList = formUserDataService.list(new QueryWrapper<FormUserData>().lambda().eq(FormUserData::getFormId, formId));
        if (CollectionUtils.isEmpty(formUserDataList)) {
            return RestResponse.ok();
        }
        Form form = service.getById(formId);

        List<FormOptions> formOptions = formOptionsService.list(new QueryWrapper<FormOptions>().lambda().eq(FormOptions::getFormId, formId));
        Map<Integer, FormOptions> formOptionsMap = formOptions.stream()
                .collect(Collectors.toMap(FormOptions::getId, t -> t));
        //处理答案
        for (FormUserData formUserData : formUserDataList) {
            if (formUserData.getType().equals("2")) {
                //单选
                FormOptions formOption = formOptionsMap.get(Integer.parseInt(formUserData.getAnswer().toString()));
                formUserData.setAnswer(formOption.getText());
            }
            if (formUserData.getType().equals("6")) {
                //多选
                String replace = formUserData.getAnswer().toString().replace("[", "");
                String replace1 = replace.replace("]", "");
                String replace2 = replace1.replace("\"", "");

                String[] split = replace2.split(",");
                List<String> strings = Arrays.asList(split);
                String answer = "";
                for (String an : strings) {
                    FormOptions formOption = formOptionsMap.get(Integer.parseInt(an));

                    if (StringUtils.isEmpty(answer)) {
                        answer = formOption.getText();
                    } else {
                        answer = answer + "/" + formOption.getText();
                    }
                }
                formUserData.setAnswer(answer);
            }
        }

        List<Integer> formSettingIds = formUserDataList.stream().map(FormUserData::getFormSettingId)
                .collect(Collectors.toList());
        List<FormSetting> formSettings = (List<FormSetting>) formSettingService.listByIds(formSettingIds);
        Map<Integer, FormSetting> formSettingMap = formSettings.stream()
                .collect(Collectors.toMap(FormSetting::getId, t -> t));
        List<Integer> userIds = formUserDataList.stream().map(FormUserData::getUserId)
                .collect(Collectors.toList());
        List<User> users = (List<User>) userService.listByIds(userIds);
        Map<Integer, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, t -> t));
        //根据患者分组
        Map<String, List<FormUserData>> FormUserDataMap = formUserDataList.stream()
                .collect(Collectors.groupingBy(FormUserData::getGroupId));
        for (FormUserData formUserData : formUserDataList) {
            formUserData.setUserName(userMap.get(formUserData.getUserId()).getPatientName());
            formUserData.setFormSettingName(formSettingMap.get(formUserData.getFormSettingId()).getName());
        }
        //定义表头 患者姓名
        List<List<String>> headList = new ArrayList<>();
        headList.add(Lists.newArrayList("题目名称"));
        //定义数据体
        List<List<Object>> dataList = new ArrayList<>();

        for (List<FormUserData> formUserDatas : FormUserDataMap.values()) {

            //定义表头
            headList.add(Lists.newArrayList(formUserDatas.get(0).getUserName()));

            //定义数据体
            int index = 0;
            for (FormUserData formUserData : formUserDatas) {

                int size = formUserDatas.size();

                if (dataList.size() != size) {
                    List<Object> data = new ArrayList<>();
                    data.add(formUserData.getFormSettingName());//第一列题目名称

                    data.add(formUserData.getAnswer());//答案
                    dataList.add(data);

                } else {
                    List<Object> beforeDatas = dataList.get(size - (size - index));
                    beforeDatas.add(formUserData.getAnswer());//答案

                }

                index++;


            }
        }
        int index = 0;
        for (List<FormUserData> formUserDatas : FormUserDataMap.values()) {
            //定义数据体
            Double score = 0.0;
            for (FormUserData formUserData : formUserDatas) {
                if (formUserData.getScope() != null) {
                    score = formUserData.getScope() + score;
                }
            }
            if (index == 0) {
                List<Object> data = new ArrayList<>();
                data.add("");//第一列题目名称

                data.add(score + "");//分数
                dataList.add(data);
            } else {

                List<Object> objectList = dataList.get(dataList.size() - 1);
                objectList.add(score);
            }
            index++;
        }
        try {
            ExcelUtil.writefFormExcel(response, dataList, form.getTitle(), "form", headList);

        } catch (Exception e) {
            e.printStackTrace();
        }

        return RestResponse.ok();
    }

    /**
     * 分页查询自己创建的表单 和部门下的表单
     *
     * @return
     */
    @GetMapping("/pageMyScoped")
    public RestResponse pageMyScoped() {
        User byId = userService.getById(SecurityUtils.getUser().getId());
        List<Integer> deptIds = new ArrayList<>();
        deptIds.add(1);
        deptIds.add(byId.getDeptId());
        Page<Form> page = getPage();
        LambdaQueryWrapper<Form> wrapper = Wrappers.<Form>lambdaQuery();

        wrapper.and(wq0 -> wq0.eq(Form::getCreateUserId, SecurityUtils.getUser().getId())
                .eq(Form::getStatus, 0));
        wrapper.or(wq0 -> wq0.in(Form::getDeptId, deptIds).eq(Form::getPlatform, 1)
                .eq(Form::getStatus, 0));

        wrapper.orderByDesc(Form::getCreateTime);
        IPage<Form> formIPage = service.page(page, wrapper);
        return RestResponse.ok(formIPage);
    }

    /**
     * 根据id查询详情
     * strId: chatMsgId 随访id
     *
     * @return
     */
    @GetMapping("/getById")
    public RestResponse getById(@RequestParam(value = "patientUserId", required = false) Integer patientUserId, @RequestParam("id") Integer id, @RequestParam(value = "strId", required = false) String strId) {
        Form byId = service.getById(id);
        ChatMsg byId1 = chatMsgService.getById(strId);
        FollowUpPlanNotice followUpPlanNotice = followUpPlanNoticeService.getById(strId);
        List<FormSetting> list = formSettingService.list(new QueryWrapper<FormSetting>().lambda().eq(FormSetting::getFormId, id));
        List<Integer> formSettingIds = list.stream().map(FormSetting::getId)
                .collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(formSettingIds)) {
            List<FormOptions> formOptions = formOptionsService.list(new QueryWrapper<FormOptions>().lambda()
                    .in(FormOptions::getFormSettingId, formSettingIds));
            if (!CollectionUtils.isEmpty(formOptions)) {
                Map<Integer, List<FormOptions>> map = formOptions.stream()
                        .collect(Collectors.groupingBy(FormOptions::getFormSettingId));
                for (FormSetting formSetting : list) {
                    formSetting.setFormOptionsList(map.get(formSetting.getId()));
                }
            }
        }

        if (byId != null) {
            byId.setFormSettings(list);
            //查询用户填写的表单数据
            //题目数据
            if (patientUserId == null) {
                patientUserId = SecurityUtils.getUser().getId();
            }
            Integer doctorId = byId.getCreateUserId();
            if (byId1 != null) {
                doctorId = byId1.getFromUid();

            } else if (followUpPlanNotice != null) {
                doctorId = followUpPlanNotice.getDoctorId();
            }
            LambdaQueryWrapper<FormUserData> eq = new QueryWrapper<FormUserData>().lambda()
                    .eq(FormUserData::getFormId, id)
                    .eq(FormUserData::getUserId, patientUserId)
                    .eq(FormUserData::getDoctorId, doctorId);
            if (!StringUtils.isEmpty(strId)) {
                eq.eq(FormUserData::getStr, strId);
            }
            List<FormUserData> formUserDataList = formUserDataService.list(eq);
            if (!CollectionUtils.isEmpty(formUserDataList)) {
                Double scope = 0.0;
                for (FormUserData formUserData : formUserDataList) {
                    if (formUserData.getScope() != null) {
                        scope = formUserData.getScope() + scope;
                    }

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
                List<FormUserData> collect = formUserDataList.stream()
                        .sorted(Comparator.comparing(FormUserData::getFormSettingId)).collect(Collectors.toList());
                byId.setFormUserDataList(collect);
                byId.setScope(scope);
            } else {
                byId.setFormUserDataList(new ArrayList<>());
            }

        }

        return RestResponse.ok(byId);
    }

    @Override
    protected Class<Form> getEntityClass() {
        return Form.class;
    }

}
