package cn.cuptec.faros.mapper;

import cn.cuptec.faros.config.datascope.DataScope;
import cn.cuptec.faros.entity.ProductStock;
import cn.cuptec.faros.entity.Protocol;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface ProtocolMapper extends BaseMapper<Protocol>{

    @Select("SELECT protocol.* FROM protocol " +
            "${ew.customSqlSegment} ORDER BY create_date DESC")
    IPage<Protocol> pageData(IPage page, @Param(Constants.WRAPPER) Wrapper wrapper, DataScope dataScope);
}
