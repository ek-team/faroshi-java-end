package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.Form;
import cn.cuptec.faros.entity.FormUserData;
import cn.cuptec.faros.service.FormService;
import cn.cuptec.faros.service.FormUserDataService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户填写的表单数据
 */
@RestController
@RequestMapping("/formUserData")
public class FormUserDataController extends AbstractBaseController<FormUserDataService, FormUserData> {
    @Resource
    private FormService formService;

    /**
     * 添加表单内容
     */
    @PostMapping("/saveData")
    public RestResponse saveData(@RequestBody List<FormUserData> formManagementData) {
        Integer formId = formManagementData.get(0).getFormId();
        Form byId = formService.getById(formId);

        service.remove(new QueryWrapper<FormUserData>().lambda()
                .eq(FormUserData::getOrderId, formManagementData.get(0).getOrderId())
                .eq(FormUserData::getUserId, SecurityUtils.getUser().getId()));
        for (FormUserData formUserData : formManagementData) {
            formUserData.setUserId(SecurityUtils.getUser().getId());
            formUserData.setCreateTime(LocalDateTime.now());
            formUserData.setDoctorId(byId.getCreateUserId());
        }
        service.saveBatch(formManagementData);
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
    public RestResponse getDataDetailByFormId(@RequestParam(value = "formId", required = false) Integer formId,@RequestParam(value = "userId", required = false) Integer userId) {

        //题目数据
        List<FormUserData> list = service.list(new QueryWrapper<FormUserData>().lambda()
                .eq(FormUserData::getUserId, userId)
                .eq(FormUserData::getFormId, formId));
        if (CollectionUtils.isEmpty(list)) {
            return RestResponse.ok();
        }
        List<Integer> formIds = list.stream().map(FormUserData::getFormId)
                .collect(Collectors.toList());
        List<Form> formList =  formService.getByIds(formIds);
        Map<Integer, List<FormUserData>> map = list.stream()
                .collect(Collectors.groupingBy(FormUserData::getFormId));
        for (Form form : formList) {
            List<FormUserData> formUserDataList = map.get(form.getId());
            List<FormUserData> collect = formUserDataList.stream()
                    .sorted(Comparator.comparing(FormUserData::getFormSettingId)).collect(Collectors.toList());
            form.setFormUserDataList(collect);

        }
        return RestResponse.ok(formList);
    }

    @Override
    protected Class<FormUserData> getEntityClass() {
        return FormUserData.class;
    }

}
