package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.SysJobLog;
import cn.cuptec.faros.mapper.SysJobLogMapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 定时任务执行日志表
 */
@Service
public class SysJobLogService extends ServiceImpl<SysJobLogMapper, SysJobLog> {

}
