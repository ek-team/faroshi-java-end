package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.Dept;
import cn.cuptec.faros.entity.DeviceLog;
import cn.cuptec.faros.mapper.DeptMapper;
import cn.cuptec.faros.mapper.DeviceLogMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class DeviceLogService extends ServiceImpl<DeviceLogMapper, DeviceLog> {
}
