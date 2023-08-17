package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.DataCount;
import cn.cuptec.faros.entity.DataType;
import cn.cuptec.faros.mapper.DataCountMapper;
import cn.cuptec.faros.mapper.DataTypeMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

@Service
public class DataTypeService extends ServiceImpl<DataTypeMapper, DataType> {
}
