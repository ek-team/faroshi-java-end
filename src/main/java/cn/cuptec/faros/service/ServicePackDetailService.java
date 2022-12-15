package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.ServicePackDetail;
import cn.cuptec.faros.entity.ServicePackageInfo;
import cn.cuptec.faros.mapper.ServicePackDetailMapper;
import cn.cuptec.faros.mapper.ServicePackageInfoMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ServicePackDetailService extends ServiceImpl<ServicePackDetailMapper, ServicePackDetail> {
}
