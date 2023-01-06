package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.Diseases;
import cn.cuptec.faros.mapper.DiseasesMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class DiseasesService extends ServiceImpl<DiseasesMapper, Diseases> {
}
