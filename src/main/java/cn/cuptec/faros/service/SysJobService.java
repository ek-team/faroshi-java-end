package cn.cuptec.faros.service;

/**
 * @Description:
 * @Author mby
 * @Date 2021/8/6 10:05
 */

import cn.cuptec.faros.entity.SysJob;
import cn.cuptec.faros.mapper.SysJobMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 定时任务调度表
 */
@Service
public class SysJobService extends ServiceImpl<SysJobMapper, SysJob> {

}
