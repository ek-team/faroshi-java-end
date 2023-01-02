package cn.cuptec.faros.mapper;

import cn.cuptec.faros.config.datascope.DataScope;
import cn.cuptec.faros.entity.DoctorTeam;
import cn.cuptec.faros.entity.ServicePack;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

public interface DoctorTeamMapper  extends BaseMapper<DoctorTeam> {
    @Select("SELECT doctor_team.*,hospital_info.name as hospitalName " +
            "FROM doctor_team " +

            "LEFT JOIN dept ON doctor_team.dept_id = dept.id " +
            "LEFT JOIN hospital_info ON doctor_team.hospital_id = hospital_info.id " +
            "${ew.customSqlSegment}  ORDER BY doctor_team.create_time,doctor_team.status DESC")
    IPage<DoctorTeam> pageScoped(IPage page, @Param(Constants.WRAPPER) Wrapper wrapper, DataScope dataScope);

}
