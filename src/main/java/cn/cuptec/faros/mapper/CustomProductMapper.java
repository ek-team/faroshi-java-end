package cn.cuptec.faros.mapper;

import cn.cuptec.faros.config.datascope.DataScope;
import cn.cuptec.faros.entity.CustomProduct;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface CustomProductMapper extends BaseMapper<CustomProduct>{

    @Select("SELECT custom_product.*, dept.name as deptName, product.product_pic, product.product_name, product.product_type from custom_product " +
            "LEFT JOIN product ON custom_product.product_id = product.id LEFT JOIN dept ON custom_product.dept_id = dept.id ${ew.customSqlSegment} ORDER BY custom_product.create_time desc")
    IPage<CustomProduct> pageScopedCustomProduct(IPage page, @Param(Constants.WRAPPER) Wrapper wrapper, DataScope dataScope);

}
