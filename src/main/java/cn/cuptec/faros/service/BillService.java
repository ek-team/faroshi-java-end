package cn.cuptec.faros.service;


import cn.cuptec.faros.entity.Bill;
import cn.cuptec.faros.mapper.BillMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class BillService extends ServiceImpl<BillMapper, Bill> {
}
