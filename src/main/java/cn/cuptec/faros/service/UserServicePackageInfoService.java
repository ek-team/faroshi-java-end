package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.User;
import cn.cuptec.faros.entity.UserServicePackageInfo;
import cn.cuptec.faros.mapper.UserMapper;
import cn.cuptec.faros.mapper.UserServicePackageInfoMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class UserServicePackageInfoService extends ServiceImpl<UserServicePackageInfoMapper, UserServicePackageInfo> {
}
