package cn.cuptec.faros.controller;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.common.constrants.CacheConstants;
import cn.cuptec.faros.common.constrants.CommonConstants;
import cn.cuptec.faros.common.utils.MapperUtil;
import cn.cuptec.faros.common.utils.TreeUtil;
import cn.cuptec.faros.controller.base.AbstractBaseController;
import cn.cuptec.faros.entity.City;
import cn.cuptec.faros.service.CityService;
import cn.cuptec.faros.vo.CityTreeNode;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @Description:
 * @Author mby
 * @Date 2020/8/12 17:21
 */
@RestController
@RequestMapping("city")
public class CityController extends AbstractBaseController<CityService, City> {

    @GetMapping("/tree")
    @Cacheable(value = CacheConstants.CITY_LIST)
    public RestResponse tree(){
        List<City> cities = service.list();
        List<CityTreeNode> cityTreeNodes = cities.stream().map(city -> {
            CityTreeNode cityTreeNode = new CityTreeNode();
            MapperUtil.populate(city, cityTreeNode);
            return cityTreeNode;
        }).collect(Collectors.toList());
        List<CityTreeNode> treeNodes = TreeUtil.build(cityTreeNodes, CommonConstants.TREE_ROOT_ID);
        return RestResponse.ok(treeNodes);
    }

    @GetMapping("/getById/{id}")
    public RestResponse getById(@PathVariable int id){
        return RestResponse.ok(service.getById(id));
    }

    @GetMapping
    public RestResponse listCity(@RequestParam("lng") double lng, @RequestParam("lat") double lat){
        return RestResponse.ok(service.listCity(lng,lat));
    }

    @Override
    protected Class<City> getEntityClass() {
        return City.class;
    }

}
