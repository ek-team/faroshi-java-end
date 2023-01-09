package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.Inquiry;
import cn.cuptec.faros.entity.LiveQrCode;
import cn.cuptec.faros.mapper.InquiryMapper;
import cn.cuptec.faros.mapper.LiveQrCodeMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class InquiryService extends ServiceImpl<InquiryMapper, Inquiry> {
}
