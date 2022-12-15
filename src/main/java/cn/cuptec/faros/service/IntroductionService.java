package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.Icons;
import cn.cuptec.faros.entity.Introduction;
import cn.cuptec.faros.mapper.IconsMapper;
import cn.cuptec.faros.mapper.IntroductionMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class IntroductionService extends ServiceImpl<IntroductionMapper, Introduction> {
}
