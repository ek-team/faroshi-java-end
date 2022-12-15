package cn.cuptec.faros.mapper;

import cn.cuptec.faros.config.datascope.DataScope;
import cn.cuptec.faros.entity.FlittingOrder;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.io.Serializable;
import java.util.List;

public interface FlittingOrderMapper extends BaseMapper<FlittingOrder> {

    @Select("SELECT flitting_order.*, dept.name as deptName, locator.locator_name, user.nickname as createrName FROM flitting_order " +
            "LEFT JOIN dept ON flitting_order.dept_id = dept.id " +
            "LEFT JOIN locator ON flitting_order.locator_id = locator.id " +
            "LEFT JOIN user ON flitting_order.create_by = user.id " +
            "${ew.customSqlSegment} " +
            "ORDER BY flitting_order.create_time DESC")
    IPage<FlittingOrder> pageScoped(IPage page, @Param(Constants.WRAPPER) Wrapper wrapper, DataScope dataScope);

    @Select("SELECT flitting_order.*, locator.locator_name, user.nickname as createrName FROM flitting_order " +
            "LEFT JOIN locator ON flitting_order.locator_id = locator.id " +
            "LEFT JOIN user ON flitting_order.create_by = user.id " +
            "${ew.customSqlSegment} ORDER BY create_time DESC")
    List<FlittingOrder> listScoped(@Param(Constants.WRAPPER) Wrapper wrapper, DataScope dataScope);

    @Select("SELECT flitting_order.*, locator.locator_name, dept.name as deptName, user.nickname as createrName FROM flitting_order " +
            "LEFT JOIN locator ON flitting_order.locator_id = locator.id " +
            "LEFT JOIN dept ON flitting_order.dept_id = dept.id " +
            "LEFT JOIN user ON flitting_order.create_by = user.id " +
            "WHERE flitting_order.id = #{id}")
    FlittingOrder getDetail(Serializable id);
}
