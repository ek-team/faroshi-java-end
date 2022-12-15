package cn.cuptec.faros.service;


import cn.cuptec.faros.entity.Address;
import cn.cuptec.faros.mapper.AddressMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class AddressService extends ServiceImpl<AddressMapper, Address> {
}
