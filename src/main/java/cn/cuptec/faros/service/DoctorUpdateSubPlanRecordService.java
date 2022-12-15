package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.DeviceScanSignLog;
import cn.cuptec.faros.entity.DoctorUpdateSubPlanRecord;
import cn.cuptec.faros.mapper.DeviceScanSignLogMapper;
import cn.cuptec.faros.mapper.DoctorUpdateSubPlanRecordMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 医生修改患者计划记录
 */
@Service
public class DoctorUpdateSubPlanRecordService extends ServiceImpl<DoctorUpdateSubPlanRecordMapper, DoctorUpdateSubPlanRecord> {
}
