package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.ElectronicCase;
import cn.cuptec.faros.entity.ElectronicInvoice;
import cn.cuptec.faros.mapper.ElectronicCaseMapper;
import cn.cuptec.faros.mapper.ElectronicInvoiceMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class ElectronicCaseService extends ServiceImpl<ElectronicCaseMapper, ElectronicCase> {
}
