package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.Locator;
import cn.cuptec.faros.entity.LogisticsSetting;
import cn.cuptec.faros.mapper.LocatorMapper;
import cn.cuptec.faros.mapper.LogisticsSettingMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class LogisticsSettingService extends ServiceImpl<LogisticsSettingMapper, LogisticsSetting> {
}
