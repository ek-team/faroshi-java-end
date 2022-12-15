package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.ServicePackProductSpec;
import cn.cuptec.faros.entity.ServicePackSaleSpec;
import cn.cuptec.faros.mapper.ServicePackProductSpecMapper;
import cn.cuptec.faros.mapper.ServicePackSaleSpecMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ServicePackSaleSpecService  extends ServiceImpl<ServicePackSaleSpecMapper, ServicePackSaleSpec> {
}
