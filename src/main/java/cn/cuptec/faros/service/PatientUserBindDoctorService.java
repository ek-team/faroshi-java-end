package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.PatientUserBindDoctor;
import cn.cuptec.faros.entity.PatientUserDiseases;
import cn.cuptec.faros.mapper.PatientUserBindDoctorMapper;
import cn.cuptec.faros.mapper.PatientUserDiseasesMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class PatientUserBindDoctorService extends ServiceImpl<PatientUserBindDoctorMapper, PatientUserBindDoctor> {
}
