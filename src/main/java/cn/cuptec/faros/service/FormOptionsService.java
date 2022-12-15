package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.Form;
import cn.cuptec.faros.entity.FormOptions;
import cn.cuptec.faros.mapper.FormMapper;
import cn.cuptec.faros.mapper.FormOptionsMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class FormOptionsService extends ServiceImpl<FormOptionsMapper, FormOptions> {
}
