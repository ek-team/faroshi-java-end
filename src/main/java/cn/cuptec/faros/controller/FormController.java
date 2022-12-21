package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.service.FormOptionsService;
import cn.cuptec.faros.service.FormService;
import cn.cuptec.faros.service.FormSettingService;
import cn.cuptec.faros.service.UserService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
            if(!CollectionUtils.isEmpty(formOptionsList)){
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
        Form form=new Form();
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
     * 根据id查询详情
     *
     * @return
     */
    @GetMapping("/getById")
    public RestResponse getById(@RequestParam("id") Integer id) {
        Form byId = service.getById(id);
        List<FormSetting> list = formSettingService.list(new QueryWrapper<FormSetting>().lambda().eq(FormSetting::getFormId, id));
        List<Integer> formSettingIds = list.stream().map(FormSetting::getId)
                .collect(Collectors.toList());
        List<FormOptions> formOptions = formOptionsService.list(new QueryWrapper<FormOptions>().lambda()
                .in(FormOptions::getFormSettingId, formSettingIds));
        if (!CollectionUtils.isEmpty(formOptions)) {
            Map<Integer, List<FormOptions>> map = formOptions.stream()
                    .collect(Collectors.groupingBy(FormOptions::getFormSettingId));
            for (FormSetting formSetting : list) {
                formSetting.setFormOptionsList(map.get(formSetting.getId()));
            }
        }
        byId.setFormSettings(list);
        return RestResponse.ok(byId);
    }

    @Override
    protected Class<Form> getEntityClass() {
        return Form.class;
    }

}
