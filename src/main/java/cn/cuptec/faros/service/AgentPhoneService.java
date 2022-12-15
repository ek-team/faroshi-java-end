package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.ActivationCodeRecord;
import cn.cuptec.faros.entity.AgentPhone;
import cn.cuptec.faros.mapper.ActivationCodeRecordMapper;
import cn.cuptec.faros.mapper.AgentPhoneMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class AgentPhoneService extends ServiceImpl<AgentPhoneMapper, AgentPhone> {
}
