package cn.cuptec.faros.mapper;

import cn.cuptec.faros.entity.Dept;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * <p>
 * 部门管理 Mapper 接口
 * </p>
 */
public interface DeptMapper extends BaseMapper<Dept> {

    @Update("<script>" +
                "<foreach collection=\"ids\" index=\"index\" item=\"item\" open=\"\" separator=\";\" close=\"\">" +
                    " update dept set sort = #{index} where id = #{item} " +
                "</foreach> " +
            "</script>")
    void autoSort(@Param("ids") List<Integer> nodeIds);

}
