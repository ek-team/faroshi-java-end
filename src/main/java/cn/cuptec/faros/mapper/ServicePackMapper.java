package cn.cuptec.faros.mapper;

import cn.cuptec.faros.config.datascope.DataScope;
import cn.cuptec.faros.entity.ServicePack;
import cn.cuptec.faros.entity.UserOrder;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface ServicePackMapper extends BaseMapper<ServicePack> {
    @Select("SELECT service_pack.*,hospital_info.name as hospitalName" +
            " FROM service_pack " +
            "LEFT JOIN hospital_info ON service_pack.hospital_id = hospital_info.id " +
            "LEFT JOIN dept ON service_pack.dept_id = dept.id " +
            "${ew.customSqlSegment} ORDER BY service_pack.create_time DESC")
    IPage<ServicePack> pageScoped(IPage page, @Param(Constants.WRAPPER) Wrapper wrapper);


    @Select("SELECT service_pack.name,service_pack.id,service_pack.dept_id " +
            " FROM service_pack " +
            "LEFT JOIN dept ON service_pack.dept_id = dept.id " +
            "${ew.customSqlSegment} ORDER BY service_pack.create_time DESC")
    List<ServicePack> listScoped(@Param(Constants.WRAPPER) Wrapper wrapper, DataScope dataScope);

}
