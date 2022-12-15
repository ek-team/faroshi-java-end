package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.SaleSpec;
import cn.cuptec.faros.entity.ServicePackageInfo;
import cn.cuptec.faros.mapper.SaleSpecMapper;
import cn.cuptec.faros.mapper.ServicePackageInfoMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class SaleSpecService extends ServiceImpl<SaleSpecMapper, SaleSpec> {
}
