package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.LiveQrCode;
import cn.cuptec.faros.entity.LoginLog;
import cn.cuptec.faros.mapper.LiveQrCodeMapper;
import cn.cuptec.faros.mapper.LoginLogMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class LoginLogService extends ServiceImpl<LoginLogMapper, LoginLog> {
}
