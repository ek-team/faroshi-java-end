package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.FlittingOrder;
import cn.cuptec.faros.entity.FlittingOrderItem;
import cn.cuptec.faros.mapper.FlittingOrderItemMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FlittingOrderItemService extends ServiceImpl<FlittingOrderItemMapper, FlittingOrderItem> {

    @Override
    public List<FlittingOrderItem> list(Wrapper<FlittingOrderItem> queryWrapper) {
        return baseMapper.listDetail(queryWrapper);
    }

    public Integer updateAlreadyFlitCount(Integer id, int size) {
        return baseMapper. updateAlreadyFlitCount( id,  size);
    }
}
