package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.ServicePack;
import cn.cuptec.faros.entity.ServicePackProductSpec;
import cn.cuptec.faros.mapper.ServicePackMapper;
import cn.cuptec.faros.mapper.ServicePackProductSpecMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ServicePackProductSpecService extends ServiceImpl<ServicePackProductSpecMapper, ServicePackProductSpec> {
}
