package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.PatientUserDiseases;
import cn.cuptec.faros.mapper.PatientUserDiseasesMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class PatientUserDiseasesService extends ServiceImpl<PatientUserDiseasesMapper, PatientUserDiseases> {
}
