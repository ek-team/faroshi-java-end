package cn.cuptec.faros.mapper;

import cn.cuptec.faros.entity.ChatUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.security.core.parameters.P;

import java.util.List;

public interface ChatUserMapper  extends BaseMapper<ChatUser> {
    @Select("<script>" +
            " SELECT * FROM `chat_user` WHERE group_type =0 and uid =#{uid}"+
             "<if test='userIds!=null and userIds.size >0 '>"
            + " and (target_uid in (2281)) "
            + "</if>" +
            "</script>")
   List<ChatUser> searchChatUser(@Param("userIds")List<Integer> userIds,
                                 @Param("uid") Integer uid);
}
