package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.ChatUser;
import cn.cuptec.faros.entity.DeliverySetting;
import cn.cuptec.faros.mapper.ChatUserMapper;
import cn.cuptec.faros.mapper.DeliverySettingMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class DeliverySettingService extends ServiceImpl<DeliverySettingMapper, DeliverySetting> {
}
