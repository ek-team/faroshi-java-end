package cn.cuptec.faros.service;

import cn.cuptec.faros.entity.PurchaseOrderItem;
import cn.cuptec.faros.mapper.PurchaseOrderItemMapper;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class PurchaseOrderItemService extends ServiceImpl<PurchaseOrderItemMapper, PurchaseOrderItem> {

    public List<PurchaseOrderItem> listDetail(Wrapper<PurchaseOrderItem> wrapper) {
        return baseMapper.listDetail(wrapper);
    }
}
