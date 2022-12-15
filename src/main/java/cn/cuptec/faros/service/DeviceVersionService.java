package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.DeviceVersion;
import cn.cuptec.faros.mapper.DeviceVersionMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeviceVersionService extends ServiceImpl<DeviceVersionMapper, DeviceVersion> {

    public DeviceVersion newVersion(Integer type) {
        return baseMapper.newVersion(type);
    }
    public List<Integer> groupByType() {
        return baseMapper.groupByType();
    }
}