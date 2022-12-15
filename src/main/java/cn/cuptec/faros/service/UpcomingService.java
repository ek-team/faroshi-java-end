package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.Upcoming;
import cn.cuptec.faros.mapper.UpcomingMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * 待办事项
 */
@Service
public class UpcomingService extends ServiceImpl<UpcomingMapper, Upcoming> {
}
