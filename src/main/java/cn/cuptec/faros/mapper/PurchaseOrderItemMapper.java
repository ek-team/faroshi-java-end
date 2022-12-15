package cn.cuptec.faros.mapper;

import cn.cuptec.faros.entity.PurchaseOrderItem;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface PurchaseOrderItemMapper extends BaseMapper<PurchaseOrderItem> {

    @Select("SELECT purchase_order_item.*, product.product_name as productName FROM purchase_order_item LEFT JOIN product ON purchase_order_item.product_id = product.id ${ew.customSqlSegment}")
    List<PurchaseOrderItem> listDetail(@Param(Constants.WRAPPER) Wrapper<PurchaseOrderItem> wrapper);
}
