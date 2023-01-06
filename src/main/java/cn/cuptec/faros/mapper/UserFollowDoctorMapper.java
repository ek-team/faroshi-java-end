package cn.cuptec.faros.mapper;

import cn.cuptec.faros.entity.UserFollowDoctor;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface UserFollowDoctorMapper extends BaseMapper<UserFollowDoctor> {

    @Select("SELECT user_follow_doctor.*,user.nickname,user.avatar " +
            "from user_follow_doctor " +
            "LEFT JOIN user on user.id=user_follow_doctor.user_id " +
            "WHERE user_follow_doctor.doctor_id =#{doctorId} " +
            "ORDER BY CONVERT(user.nickname USING GBK) desc limit #{pageNum},#{pageSize} "
    )

    List<UserFollowDoctor> pageQueryPatientUserSort(@Param("pageNum") Integer pageNum,@Param("pageSize") Integer pageSize,@Param("doctorId") Integer doctorId );

    @Select("SELECT count(1) " +
            "from user_follow_doctor " +
            "LEFT JOIN user on user.id=user_follow_doctor.user_id " +
            "WHERE user_follow_doctor.doctor_id =#{doctorId} "
    )
   int pageQueryPatientUserSortTotal(@Param("doctorId") Integer doctorId );
}
