package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.FollowUpPlan;
import cn.cuptec.faros.entity.FollowUpPlanPatientUser;
import cn.cuptec.faros.mapper.FollowUpPlanMapper;
import cn.cuptec.faros.mapper.FollowUpPlanPatientUserMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class FollowUpPlanPatientUserService extends ServiceImpl<FollowUpPlanPatientUserMapper, FollowUpPlanPatientUser> {
}
