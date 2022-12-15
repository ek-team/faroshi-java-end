package cn.cuptec.faros.service;

import cn.cuptec.faros.common.constrants.CommonConstants;
import cn.cuptec.faros.common.utils.StringUtils;
import cn.cuptec.faros.config.datascope.DataScope;
import cn.cuptec.faros.config.security.util.SecurityUtils;
import cn.cuptec.faros.entity.*;
import cn.cuptec.faros.mapper.LocatorMapper;
import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LocatorService extends ServiceImpl<LocatorMapper, Locator> {


    @Resource
    private DeptService deptService;


    @Resource
    private CityService cityService;

    @Resource
    private UserRoleService userRoleService;
    @Resource
    private UserService userService;

    @Override
    public boolean save(Locator entity) {
        entity = setRegion(entity);
        return super.save(entity);
    }

    @Override
    public boolean updateById(Locator entity) {
        entity = setRegion(entity);
        return super.updateById(entity);
    }

    public boolean updateLocator(Locator entity) {
        return super.updateById(entity);
    }

    public IPage pageScoped(IPage page, QueryWrapper<Locator> wrapper) {
        Integer id = SecurityUtils.getUser().getId();
        User byId = userService.getById(id);
        if(byId == null ) return  page.setRecords(CollUtil.toList());

        Boolean isAdmin = userRoleService.judgeUserIsAdmin(id);

        if(!isAdmin){


            wrapper.and(w->{
                w.eq("locator_type",1);
                    w.or(byId.getDeptId()!=null);
                    w.eq("locator_type",2);
                    w.eq("dept_id",byId.getDeptId());

                return w;
            });




        }


        IPage<Locator> locatorIPage = baseMapper.pageScoped(page, wrapper, null);
        return page;
    }

    public List<Locator> listScoped(QueryWrapper<Locator> wrapper) {
        Integer id = SecurityUtils.getUser().getId();
        User byId = userService.getById(id);
        if(byId == null ) return  CollUtil.toList();

        Boolean isAdmin = userRoleService.judgeUserIsAdmin(id);

        if(!isAdmin){


            wrapper.and(w->{
                w.eq("locator_type",1);
                w.or(byId.getDeptId()!=null);
                w.eq("locator_type",2);
                w.eq("dept_id",byId.getDeptId());

                return w;
            });


        }
        return super.list(wrapper);
    }

    private Locator setRegion(Locator locator) {
        Integer[] locatorRegionIds = locator.getLocatorRegions();
        if (locatorRegionIds != null && locatorRegionIds.length > 0) {
            List<String> regionNameList = new ArrayList<>();
            for (int i = 0; i < locatorRegionIds.length; i++) {
                City city = cityService.getById(locatorRegionIds[i]);
                regionNameList.add(city.getName());
            }
            locator.setLocatorRegion(StringUtils.join(regionNameList, CommonConstants.VALUE_SEPARATOR));
        }
        return locator;
    }

    public List<Locator> superListScoped( Integer deptId) {
        if(deptId != null){
            Dept parentDept = deptService.getParentDept(deptId);
            if(parentDept != null){
                return listBydeptId(parentDept.getId());
            }

        }


        return CollUtil.toList();
    }

    public List<Locator> listBydeptId(Integer deptId) {

        return this.list(new QueryWrapper<Locator>().lambda().eq(Locator::getDeptId,deptId));
    }
}
