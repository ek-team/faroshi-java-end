package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.TbTrainData;
import cn.cuptec.faros.entity.TimeData;
import cn.cuptec.faros.mapper.TimeDataMapper;
import cn.cuptec.faros.mapper.TrainDataMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class TimeDataService extends ServiceImpl<TimeDataMapper, TimeData> {
}
