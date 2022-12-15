package cn.cuptec.faros.mapper;

import cn.cuptec.faros.entity.ImUserRoomRelation;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface ImUserRoomRelationMapper extends BaseMapper<ImUserRoomRelation> {



    @Update("<script>" +
            "UPDATE im_user_room_relation SET unread_num = unread_num +  #{sumNum}  " +
            " WHERE room_id = #{roomId}" +
            "<if test=\"fromUserId != null\">" +
            " and user_id &lt;&gt; #{fromUserId}" +
            "</if> " +
            "</script>")
    void updateSumUnreadNum(@Param("roomId") Integer roomId, @Param("sumNum") Integer sumNum,@Param("fromUserId") Integer fromUserId);
}