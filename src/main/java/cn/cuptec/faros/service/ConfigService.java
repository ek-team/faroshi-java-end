package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.City;
import cn.cuptec.faros.entity.Config;
import cn.cuptec.faros.mapper.CityMapper;
import cn.cuptec.faros.mapper.ConfigMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * @Description:
 * @Author mby
 * @Date 2021/8/5 18:03
 */
@Service
public class ConfigService extends ServiceImpl<ConfigMapper, Config> {

    /**
     * 根据类型查询
     */
    public Config getByType(int type) {
        return getByType(type);
    }

    //根据id修改
    public void updateDayTime(int id, int dayTime) {
        Config config = new Config();
        config.setId(id);
        config.setDayTime(dayTime);
        updateById(config);
    }
}
