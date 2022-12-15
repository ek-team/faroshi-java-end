package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.EvaluationRecords;
import cn.cuptec.faros.entity.LiveQrCode;
import cn.cuptec.faros.mapper.EvaluationRecordsMapper;
import cn.cuptec.faros.mapper.LiveQrCodeMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class EvaluationRecordsService extends ServiceImpl<EvaluationRecordsMapper, EvaluationRecords> {
}
