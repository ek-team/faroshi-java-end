package cn.cuptec.faros.mapper;

import cn.cuptec.faros.entity.DeviceVersion;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface DeviceVersionMapper extends BaseMapper<DeviceVersion> {

    @Select(
            "<script>"
                + "select a.* from device_version a " +
                   "where " +
                        " type = #{type}  and " +
                        "  is_del = 0  " +
                        " ORDER BY CAST(version AS UNSIGNED) desc, version desc " +
                    " limit 1 "
            + "</script>"
                    )
    DeviceVersion newVersion(@Param("type") Integer type);
    @Select(
            "<script>"
                    + "select type from device_version  " +
                    "group by type "
                    + "</script>"
    )
   List<Integer> groupByType();
}