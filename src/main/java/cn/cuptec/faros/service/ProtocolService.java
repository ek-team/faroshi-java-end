package cn.cuptec.faros.service;

import cn.cuptec.faros.config.datascope.DataScope;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.entity.Protocol;
import cn.cuptec.faros.entity.UserRole;
import cn.cuptec.faros.mapper.ProtocolMapper;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProtocolService extends ServiceImpl<ProtocolMapper, Protocol> {

    @Resource
    private UserRoleService userRoleService;

    public IPage<Protocol> pageData(IPage page, Wrapper wrapper) {
        DataScope dataScope = new DataScope();
        dataScope.setIsOnly(true);
        List<UserRole> listByUserId = userRoleService.getListByUserId(SecurityUtils.getUser().getId());
        if(CollectionUtils.isNotEmpty(listByUserId)){
            List<Integer> collect = listByUserId.stream().map(UserRole::getRoleId).collect(Collectors.toList());
            if(collect.contains(7))  dataScope.setIsOnly(false);
        }

        IPage<Protocol> pageResult = baseMapper.pageData(page, wrapper, dataScope);

        return pageResult;
    }
}
