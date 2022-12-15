package cn.cuptec.faros.mapper;

import cn.cuptec.faros.config.datascope.DataScope;
import cn.cuptec.faros.entity.Locator;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface LocatorMapper extends BaseMapper<Locator> {

    @Select("SELECT locator.*, dept.name as deptName FROM locator LEFT JOIN dept ON locator.dept_id = dept.id ${ew.customSqlSegment} ORDER BY create_time DESC")
    IPage<Locator> pageScoped(IPage page, @Param(Constants.WRAPPER) Wrapper wrapper, DataScope dataScope);

    @Select("SELECT * FROM locator ${ew.customSqlSegment} ORDER BY create_time DESC")
    List<Locator> listScoped(@Param(Constants.WRAPPER) Wrapper queryWrapper, DataScope dataScope);
}
