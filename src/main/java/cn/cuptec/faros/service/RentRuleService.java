package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.RecyclingRule;
import cn.cuptec.faros.entity.RentRule;
import cn.cuptec.faros.mapper.RecyclingRuleMapper;
import cn.cuptec.faros.mapper.RentRuleMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class RentRuleService extends ServiceImpl<RentRuleMapper, RentRule> {
}
