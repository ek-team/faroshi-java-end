package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.DeptCity;
import cn.cuptec.faros.mapper.DeptCityMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;

@Service
public class DeptCityService extends ServiceImpl<DeptCityMapper, DeptCity> {

    public void updateDeptCityRelation(Integer id, List<Integer> deptCityIds) {
        if(deptCityIds != null && deptCityIds.size() > 0)
            baseMapper.updateDeptCityRelation(id, deptCityIds);
    }

    /**
     * 根据城市code查询deptId
     */
    public Integer selectByCityId(int code){
        return  baseMapper.selectByCityId(code);
    }


}
