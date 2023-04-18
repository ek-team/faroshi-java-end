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

import java.util.List;

public interface DoctorTeamMapper extends BaseMapper<DoctorTeam> {
    @Select("SELECT doctor_team.*,hospital_info.name as hospitalName " +
            "FROM doctor_team " +

            "LEFT JOIN dept ON doctor_team.dept_id = dept.id " +
            "LEFT JOIN hospital_info ON doctor_team.hospital_id = hospital_info.id " +
            "${ew.customSqlSegment}  ORDER BY doctor_team.create_time,doctor_team.status DESC")
    IPage<DoctorTeam> pageScoped(IPage page, @Param(Constants.WRAPPER) Wrapper wrapper);


//    @Select( "<script> SELECT doctor_team.dept_id_list,doctor_team.`name`,doctor_team.id from doctor_team LEFT JOIN doctor_team_people on doctor_team.id= " +
//            "doctor_team_people.team_id WHERE doctor_team_people.id is not null and doctor_team.dept_id_list " +
//            " IN " +
//            "  <foreach collection=\"deptIds\" item=\"id\" index=\"index\" open=\"(\" close=\")\" separator=\",\">\n" +
//            "   #{id}\n" +
//            "   </foreach> " +
//            "and doctor_team.status=1 </script>")


    @Select( "SELECT doctor_team.dept_id_list,doctor_team.`name`,doctor_team.id from doctor_team LEFT JOIN doctor_team_people on doctor_team.id= " +
            "doctor_team_people.team_id WHERE doctor_team_people.id is not null and doctor_team.dept_id_list LIKE CONCAT('%',#{deptId},'%') " +
            "and doctor_team.status=1")
    List<DoctorTeam> pageScopedHavePeople(@Param("deptId") String deptId);
}
