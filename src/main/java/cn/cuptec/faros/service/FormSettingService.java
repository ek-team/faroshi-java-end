package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.Form;
import cn.cuptec.faros.entity.FormSetting;
import cn.cuptec.faros.mapper.FormMapper;
import cn.cuptec.faros.mapper.FormSettingMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class FormSettingService extends ServiceImpl<FormSettingMapper, FormSetting> {
}
