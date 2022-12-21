package cn.cuptec.faros.mapper;

import cn.cuptec.faros.config.datascope.DataScope;
import cn.cuptec.faros.entity.Form;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface FormMapper extends BaseMapper<Form> {
    @Select("SELECT form.* " +
            " FROM form " +
            "LEFT JOIN dept ON form.dept_id = dept.id " +
            "${ew.customSqlSegment} and form.status = 0  ORDER BY form.create_time DESC")
    IPage<Form> pageScoped(IPage page, @Param(Constants.WRAPPER) Wrapper wrapper, DataScope dataScope);

}
