package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.DoctorUserAction;
import cn.cuptec.faros.entity.ElectronicCase;
import cn.cuptec.faros.entity.Inquiry;
import cn.cuptec.faros.service.ElectronicCaseService;
import cn.cuptec.faros.service.InquiryService;
import lombok.AllArgsConstructor;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 电子病例
 */
@RestController
@RequestMapping("/electronicCase")
@AllArgsConstructor
public class ElectronicCaseController extends AbstractBaseController<ElectronicCaseService, ElectronicCase> {
    @Resource
    private InquiryService inquiryService;//问诊单

    /**
     * 添加电子病例
     *
     * @param electronicCase
     * @return
     */
    @PostMapping("/save")
    public RestResponse updateService(@RequestBody ElectronicCase electronicCase) {
        electronicCase.setCreateTime(LocalDateTime.now());
        electronicCase.setInquiryCount(electronicCase.getInquirys().size());
        electronicCase.setFollowUpPlanCount(electronicCase.getFollowUpPlanIds().size());
        electronicCase.setCreateUserId(SecurityUtils.getUser().getId());
        List<Integer> followUpPlanIds = electronicCase.getFollowUpPlanIds();
        if (!CollectionUtils.isEmpty(followUpPlanIds)) {
            String followUpPlanIdList = "";
            for (Integer followUpPlanId : followUpPlanIds) {
                if (StringUtils.isEmpty(followUpPlanIdList)) {
                    followUpPlanIdList = followUpPlanId + "";
                } else {
                    followUpPlanIdList = followUpPlanIdList + "," + followUpPlanId;
                }
            }
            electronicCase.setFollowUpPlanIdList(followUpPlanIdList);
        }
        service.save(electronicCase);

        List<Inquiry> inquirys = electronicCase.getInquirys();
        if (!CollectionUtils.isEmpty(inquirys)) {
            for (Inquiry inquiry : inquirys) {
                inquiry.setElectronicCaseId(electronicCase.getId());

            }
            inquiryService.saveBatch(inquirys);
        }
        return RestResponse.ok();
    }


    @Override
    protected Class<ElectronicCase> getEntityClass() {
        return ElectronicCase.class;
    }
}
