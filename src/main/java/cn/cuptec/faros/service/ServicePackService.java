package cn.cuptec.faros.service;

import cn.cuptec.faros.config.datascope.DataScope;
import cn.cuptec.faros.entity.SalesmanRetrieveAddress;
import cn.cuptec.faros.entity.ServicePack;
import cn.cuptec.faros.entity.UserOrder;
import cn.cuptec.faros.mapper.SalesmanRetrieveAddressMapper;
import cn.cuptec.faros.mapper.ServicePackMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ServicePackService extends ServiceImpl<ServicePackMapper, ServicePack> {
    public IPage<ServicePack> pageScoped(Boolean admin, IPage<ServicePack> page, Wrapper<ServicePack> queryWrapper) {
        DataScope dataScope = new DataScope();
        dataScope.setIsOnly(!admin);
        return baseMapper.pageScoped(page, queryWrapper, dataScope);
    }

    public List<ServicePack> listScoped(Wrapper<ServicePack> queryWrapper) {
        DataScope dataScope = new DataScope();
        dataScope.setIsOnly(true);
        return baseMapper.listScoped(queryWrapper, dataScope);
    }
}
