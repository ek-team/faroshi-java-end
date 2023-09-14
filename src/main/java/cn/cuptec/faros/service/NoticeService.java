package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.Notice;
import cn.cuptec.faros.entity.TbTrainUser;
import cn.cuptec.faros.mapper.NoticeMapper;
import cn.cuptec.faros.mapper.PlanUserMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class NoticeService extends ServiceImpl<NoticeMapper, Notice> {
}
