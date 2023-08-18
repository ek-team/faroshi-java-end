package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.TimeData;
import cn.cuptec.faros.entity.TrainNumber;
import cn.cuptec.faros.mapper.TimeDataMapper;
import cn.cuptec.faros.mapper.TrainNumberMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class TrainNumberService extends ServiceImpl<TrainNumberMapper, TrainNumber> {
}
