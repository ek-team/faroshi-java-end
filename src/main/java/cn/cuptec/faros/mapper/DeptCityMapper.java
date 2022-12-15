package cn.cuptec.faros.mapper;

import cn.cuptec.faros.entity.DeptCity;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

public interface DeptCityMapper extends BaseMapper<DeptCity> {

  @Update(
      "<script>"
          + "DELETE FROM dept_city WHERE dept_id = #{deptId};"
          + "INSERT INTO dept_city VALUE"
          + "<foreach collection='cityIds' index='index' item='item' separator=',' open='' close=''>"
          + "(#{deptId}, #{item, jdbcType=INTEGER})"
          + "</foreach>"
          + "</script>")
  void updateDeptCityRelation(
      @Param("deptId") Integer deptId, @Param("cityIds") List<Integer> cityIds);
  @Select("SELECT dept_id FROM `dept_city` where city_id=#{code} ")
  Integer selectByCityId(int code);
}
