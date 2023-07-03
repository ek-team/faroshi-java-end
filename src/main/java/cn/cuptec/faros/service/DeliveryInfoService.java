package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.DeliveryInfo;
import cn.cuptec.faros.entity.DeliverySetting;
import cn.cuptec.faros.mapper.DeliveryInfoMapper;
import cn.cuptec.faros.mapper.DeliverySettingMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class DeliveryInfoService extends ServiceImpl<DeliveryInfoMapper, DeliveryInfo> {
}
