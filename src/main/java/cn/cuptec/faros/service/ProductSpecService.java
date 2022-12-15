package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.Product;
import cn.cuptec.faros.entity.ProductSpec;
import cn.cuptec.faros.mapper.ProductMapper;
import cn.cuptec.faros.mapper.ProductSpecMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ProductSpecService extends ServiceImpl<ProductSpecMapper, ProductSpec> {
}
