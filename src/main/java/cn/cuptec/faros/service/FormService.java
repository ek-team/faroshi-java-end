package cn.cuptec.faros.service;

import cn.cuptec.faros.config.datascope.DataScope;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.mapper.FlittingOrderMapper;
import cn.cuptec.faros.mapper.FormMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class FormService extends ServiceImpl<FormMapper, Form> {
    @Resource
    private FormSettingService formSettingService;
    @Resource
    private FormOptionsService formOptionsService;

    public IPage<Form> pageScoped(IPage<Form> page, Wrapper<Form> queryWrapper) {
        DataScope dataScope = new DataScope();
        dataScope.setIsOnly(true);
        return baseMapper.pageScoped(page, queryWrapper, dataScope);
    }

    public List<Form> getByIds(List<Integer> ids) {
        List<Form> formList = baseMapper.selectBatchIds(ids);
        //查询表单选项
        List<Integer> formIds = formList.stream().map(Form::getId)
                .collect(Collectors.toList());
        List<FormSetting> formSettingList = formSettingService.list(new QueryWrapper<FormSetting>().lambda().in(FormSetting::getFormId, formIds));
        List<Integer> formSettingIds = formSettingList.stream().map(FormSetting::getId)
                .collect(Collectors.toList());
        List<FormOptions> formOptionsList = formOptionsService.list(new QueryWrapper<FormOptions>().lambda().in(FormOptions::getFormSettingId, formSettingIds));
        Map<Integer, List<FormOptions>> formOptionsMap = formOptionsList.stream()
                .collect(Collectors.groupingBy(FormOptions::getFormSettingId));
        for (FormSetting formSetting : formSettingList) {
            formSetting.setFormOptionsList(formOptionsMap.get(formSetting.getId()));
        }
        Map<Integer, List<FormSetting>> formSettingMap = formSettingList.stream()
                .collect(Collectors.groupingBy(FormSetting::getFormId));
        for (Form form : formList) {
            List<FormSetting> formSettings = formSettingMap.get(form.getId());
            List<FormSetting> collect = formSettings.stream()
                    .sorted(Comparator.comparing(FormSetting::getId)).collect(Collectors.toList());

            form.setFormSettings(collect);
        }
        return formList;
    }
}
