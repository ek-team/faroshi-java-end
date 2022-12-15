package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.controller.base.AbstractBaseController;

import cn.cuptec.faros.entity.DoctorUpdateSubPlanRecord;
import cn.cuptec.faros.service.DoctorUpdateSubPlanRecordService;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/updateSubPlanRecord")
public class DoctorUpdateSubPlanRecordController extends AbstractBaseController<DoctorUpdateSubPlanRecordService, DoctorUpdateSubPlanRecord> {

    /**
     * 设备端修改计划 添加修改记录
     * @return
     */
    @PostMapping("/save")
    public RestResponse save(@RequestBody DoctorUpdateSubPlanRecord doctorUpdateSubPlanRecord) {
        return RestResponse.ok(service.save(doctorUpdateSubPlanRecord));
    }
    @Override
    protected Class<DoctorUpdateSubPlanRecord> getEntityClass() {
        return DoctorUpdateSubPlanRecord.class;
    }
}
