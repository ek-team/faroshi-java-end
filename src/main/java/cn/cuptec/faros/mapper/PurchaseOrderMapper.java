package cn.cuptec.faros.mapper;

import cn.cuptec.faros.config.datascope.DataScope;
import cn.cuptec.faros.entity.PurchaseOrder;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface PurchaseOrderMapper extends BaseMapper<PurchaseOrder> {

    @Select("SELECT purchase_order.*, user.nickname as salesmanName, dept.name as deptName " +
            "FROM purchase_order " +
            "LEFT JOIN user ON purchase_order.purchaser_id = user.id " +
            "LEFT JOIN dept ON purchase_order.dept_id = dept.id " +
            "${ew.customSqlSegment} " +
            "ORDER BY purchase_order.create_time DESC")
    IPage<PurchaseOrder> pageScoped(IPage page, @Param(Constants.WRAPPER) Wrapper queryWrapper, DataScope dataScope);

    @Select("SELECT * FROM purchase_order ${ew.customSqlSegment} ORDER BY create_time DESC ")
    List<PurchaseOrder> listScoped(@Param(Constants.WRAPPER) Wrapper queryWrapper, DataScope dataScope);
}
