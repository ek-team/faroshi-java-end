package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.UserFollowDoctor;
import cn.cuptec.faros.entity.UserGroupRelationUser;
import cn.cuptec.faros.mapper.UserFollowDoctorMapper;
import cn.cuptec.faros.mapper.UserGroupRelationUserMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class UserGroupRelationUserService extends ServiceImpl<UserGroupRelationUserMapper, UserGroupRelationUser> {
}
