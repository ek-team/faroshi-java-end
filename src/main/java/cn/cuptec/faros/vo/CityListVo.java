package cn.cuptec.faros.vo;

import cn.cuptec.faros.entity.City;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class CityListVo implements Serializable{

    private static final long serialVersionUID = 1L;

    private City locationCity;

    //热门城市
    private List<City> hotCityList;

    //全部城市
    private List<City> cityList;

}
