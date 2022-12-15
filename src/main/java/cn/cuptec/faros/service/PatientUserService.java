package cn.cuptec.faros.service;


import cn.cuptec.faros.entity.PatientUser;
import cn.cuptec.faros.mapper.PatientUserMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class PatientUserService extends ServiceImpl<PatientUserMapper, PatientUser> {
}
