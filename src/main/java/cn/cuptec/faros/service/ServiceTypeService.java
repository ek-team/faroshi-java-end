package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.Role;
import cn.cuptec.faros.entity.ServiceType;
import cn.cuptec.faros.mapper.RoleMapper;
import cn.cuptec.faros.mapper.ServiceTypeMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ServiceTypeService extends ServiceImpl<ServiceTypeMapper, ServiceType> {
}
