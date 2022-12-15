package cn.cuptec.faros.service;

import cn.cuptec.faros.config.datascope.DataScope;
import cn.cuptec.faros.entity.FlittingOrder;
import cn.cuptec.faros.entity.Form;
import cn.cuptec.faros.entity.ServicePack;
import cn.cuptec.faros.mapper.FlittingOrderMapper;
import cn.cuptec.faros.mapper.FormMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class FormService extends ServiceImpl<FormMapper, Form> {
    public IPage<Form> pageScoped(IPage<Form> page, Wrapper<Form> queryWrapper) {
        DataScope dataScope = new DataScope();
        dataScope.setIsOnly(true);
        return baseMapper.pageScoped(page, queryWrapper, dataScope);
    }
}
