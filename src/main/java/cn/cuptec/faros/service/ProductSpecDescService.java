package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.ProductSpec;
import cn.cuptec.faros.entity.ProductSpecDesc;
import cn.cuptec.faros.mapper.ProductSpecDescMapper;
import cn.cuptec.faros.mapper.ProductSpecMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ProductSpecDescService extends ServiceImpl<ProductSpecDescMapper, ProductSpecDesc> {
}
