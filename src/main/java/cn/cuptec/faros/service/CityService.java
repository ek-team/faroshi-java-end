package cn.cuptec.faros.service;

import cn.cuptec.faros.common.bean.AddressComponent;
import cn.cuptec.faros.common.constrants.CacheConstants;
import cn.cuptec.faros.common.utils.LocationUtil;
import cn.cuptec.faros.entity.City;
import cn.cuptec.faros.mapper.CityMapper;
import cn.cuptec.faros.vo.CityListVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CityService extends ServiceImpl<CityMapper, City> {

    /**
     * 查询热门城市及全部城市
     * @return
     */
    @Cacheable(value = CacheConstants.CITY_LIST_HOT)
    public CityListVo listCity(double lng, double lat) {
        CityListVo vo = new CityListVo();

        //全部省及全部市
        List<City> all = list(Wrappers.<City>lambdaQuery().eq(City::getLevelType, 1));
        all.forEach(city -> {
            city.setSubModelList(list(Wrappers.<City>lambdaQuery().eq(City::getParentId, city.getId())));
        });

        //热门城市
        LambdaQueryWrapper<City> hotQueryWrapper = Wrappers.<City>lambdaQuery().eq(City::getIsHot, true);//0-国  1-省 2-市 3-区
        List<City> hot = list(hotQueryWrapper);

        //当前所在城市
        AddressComponent addressComponent = LocationUtil.geocoder_baidu(lng, lat);
        if (addressComponent != null){
            City locationCity = getOne(Wrappers.<City>lambdaQuery().eq(City::getName, addressComponent.getCity())
                    .eq(City::getLevelType, 2)
            );
            vo.setLocationCity(locationCity);
        }

        vo.setCityList(all);
        vo.setHotCityList(hot);
        return vo;
    }

    /**
     * 根据城市id查询其子数据(包括本身)
     * @param cityId
     * @return
     */
    public List<City> listWithChildren(int cityId){
        City rootCity = getById(cityId);
        int depth = 3 - rootCity.getLevelType();

        List<City> all = new ArrayList<>();
        all.add(rootCity);

        List<City> tempCity = new ArrayList<>();
        tempCity.add(rootCity);

        for(int i = 0; i < depth; i ++){
            List<Integer> parentIds = tempCity.stream().map(city -> city.getId()).collect(Collectors.toList());
            List<City> cityList = list(Wrappers.<City>lambdaQuery().in(City::getId, parentIds));
            all.addAll(cityList);
            tempCity = cityList;
        }

        return all;
    }

}
