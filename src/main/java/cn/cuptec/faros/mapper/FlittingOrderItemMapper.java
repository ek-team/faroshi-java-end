package cn.cuptec.faros.mapper;

import cn.cuptec.faros.entity.FlittingOrderItem;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface FlittingOrderItemMapper extends BaseMapper<FlittingOrderItem> {

    @Select("SELECT flitting_order_item.*, product.product_name FROM flitting_order_item LEFT JOIN product ON flitting_order_item.product_id = product.id ${ew.customSqlSegment}")
    List<FlittingOrderItem> listDetail(@Param(Constants.WRAPPER) Wrapper<FlittingOrderItem> queryWrapper);
    @Update("Update flitting_order_item set already_flit_count = already_flit_count +#{alreadyFlitCount} where id =#{id} and  flit_count  >= already_flit_count + #{alreadyFlitCount} ")
    Integer updateAlreadyFlitCount(@Param("id")Integer id,@Param("alreadyFlitCount") int alreadyFlitCount);
}
