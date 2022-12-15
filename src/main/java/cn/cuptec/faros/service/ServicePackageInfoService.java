package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.ServicePack;
import cn.cuptec.faros.entity.ServicePackageInfo;
import cn.cuptec.faros.mapper.ServicePackMapper;
import cn.cuptec.faros.mapper.ServicePackageInfoMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ServicePackageInfoService extends ServiceImpl<ServicePackageInfoMapper, ServicePackageInfo> {
}
