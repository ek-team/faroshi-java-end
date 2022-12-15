package cn.cuptec.faros.mapper;

import cn.cuptec.faros.config.datascope.DataScope;
import cn.cuptec.faros.entity.RetrieveOrder;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface RetrieveOrderMapper extends BaseMapper<RetrieveOrder> {

    @Select("SELECT retrieve_order.*, dept.name as deptName, user.nickname as salesmanName from retrieve_order " +
            "LEFT JOIN dept ON retrieve_order.dept_id = dept.id " +
            "LEFT JOIN user ON retrieve_order.salesman_id = user.id " +
            "${ew.customSqlSegment} ORDER BY retrieve_order.create_time desc")
    IPage<RetrieveOrder> pageRetrieveOrder(IPage<RetrieveOrder> page, @Param(Constants.WRAPPER) Wrapper<RetrieveOrder> queryWrapper);

    @Select("SELECT retrieve_order.*, dept.name as deptName, user.nickname as salesmanName from retrieve_order " +
            "LEFT JOIN dept ON retrieve_order.dept_id = dept.id " +
            "LEFT JOIN user ON retrieve_order.salesman_id = user.id " +
            "${ew.customSqlSegment} ORDER BY retrieve_order.create_time desc")
    IPage<RetrieveOrder> pageScoped(IPage page, @Param(Constants.WRAPPER) Wrapper wrapper, DataScope dataScope);

    @Select("SELECT retrieve_order.* from retrieve_order " +
            "${ew.customSqlSegment} ORDER BY retrieve_order.create_time desc")
    List<RetrieveOrder> listScoped(@Param(Constants.WRAPPER) Wrapper wrapper, DataScope dataScope);
    @Select("SELECT ro.* from retrieve_order ro " +
            "left JOIN user_order uo " +
            "on ro.order_id = uo.id  " +
            "where ro.dept_id = #{deptId} and ro.`status` = 2 and uo.real_pay_type =2")
    IPage<RetrieveOrder>  canRefundList(IPage<RetrieveOrder> page, @Param("deptId")Integer deptId);
}
