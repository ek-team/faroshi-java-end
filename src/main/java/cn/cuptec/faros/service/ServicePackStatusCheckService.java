package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.ServicePack;
import cn.cuptec.faros.entity.ServicePackStatusCheck;
import cn.cuptec.faros.mapper.ServicePackMapper;
import cn.cuptec.faros.mapper.ServicePackStatusCheckMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ServicePackStatusCheckService extends ServiceImpl<ServicePackStatusCheckMapper, ServicePackStatusCheck> {
}
