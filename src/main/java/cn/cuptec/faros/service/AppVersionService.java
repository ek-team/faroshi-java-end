package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.AppVersion;
import cn.cuptec.faros.mapper.AppVersionMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class AppVersionService extends ServiceImpl<AppVersionMapper, AppVersion> {
}
