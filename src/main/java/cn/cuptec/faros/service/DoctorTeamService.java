package cn.cuptec.faros.service;

import cn.cuptec.faros.config.datascope.DataScope;
import cn.cuptec.faros.entity.DoctorTeam;
import cn.cuptec.faros.entity.ServicePack;
import cn.cuptec.faros.entity.ServicePackSaleSpec;
import cn.cuptec.faros.mapper.DoctorTeamMapper;
import cn.cuptec.faros.mapper.ServicePackSaleSpecMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class DoctorTeamService extends ServiceImpl<DoctorTeamMapper, DoctorTeam> {

    public IPage<DoctorTeam> pageScoped(IPage<DoctorTeam> page, Wrapper<DoctorTeam> queryWrapper) {


        return baseMapper.pageScoped(page, queryWrapper);
    }

    public List<DoctorTeam> pageScopedHavePeople(String deptId){

       return baseMapper.pageScopedHavePeople(deptId);
   }
}
