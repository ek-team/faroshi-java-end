package cn.cuptec.faros.mapper;

import cn.cuptec.faros.config.datascope.DataScope;
import cn.cuptec.faros.entity.User;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * 用户表 Mapper 接口
 */
public interface UserMapper extends BaseMapper<User> {

  /** 根据手机号保存用户，若已有，绑定微信信息 */
  @Insert(
      "<script>"
          + "INSERT INTO faros.user ( "
              + "<if test=\"phone != null\">"
                + "phone"
              + "</if>"
              + "<if test=\"unionId != null\">"
                + ", union_id"
              + "</if>"
              + "<if test=\"maOpenId != null\">"
                + ", ma_open_id"
              + "</if>"
              + "<if test=\"mpOpenId != null\">"
                + ", mp_open_id"
              + "</if>"
              + ") VALUES (#{phone}"
              + "<if test=\"unionId != null\">"
                + ", #{unionId}"
              + "</if>"
              + "<if test=\"maOpenId != null\">"
                + ", #{maOpenId}"
              + "</if>"
              + "<if test=\"mpOpenId != null\">"
                + ", #{mpOpenId}"
              + "</if>"
              + ") ON DUPLICATE KEY "
              + "<trim prefix=\"UPDATE\" suffixOverrides=\",\">  "
                  + "<if test=\"unionId != null\">"
                        + " phone = #{phone},"
                  + "</if>"
                  + "<if test=\"unionId != null\">"
                        + " union_id = #{unionId},"
                  + "</if>"
                  + "<if test=\"maOpenId != null\">"
                        + " ma_open_id = #{maOpenId},"
                  + "</if>"
                  + "<if test=\"mpOpenId != null\">"
                        + " mp_open_id = #{mpOpenId}, "
                  + "</if>" +
                    "update_time = CURRENT_TIMESTAMP" +
              "</trim>"
          + "</script>")
  int saveUserOnDuplicateKeyUpdateWxUserInfo(
      @Param("phone") String phone,
      @Param("unionId") String unionId,
      @Param("maOpenId") String maOpenId,
      @Param("mpOpenId") String mpOpenId);

	@Select("<script>" +
			"SELECT `user`.*, dept.name as deptName FROM user left JOIN dept ON user.dept_id = dept.id ${ew.customSqlSegment} " +
			"ORDER BY `user`.create_time DESC" +
			"</script>")
    IPage<User> pageScopedUser(IPage page, @Param(Constants.WRAPPER) Wrapper wrapper, DataScope dataScope);
	@Select("<script>" +
			"SELECT `user`.*, dept.name as deptName FROM user" +
			" left JOIN user_role ON user.id = user_role.user_id  " +
			" left JOIN dept ON user.dept_id = dept.id ${ew.customSqlSegment} " +
			" and  user_role.role_id is null " +
			"ORDER BY `user`.create_time DESC" +
			"</script>")
	List<User> queryUserByDeptAndNoRole(Page page ,@Param(Constants.WRAPPER) Wrapper wrapper);
	@Select("SELECT user.id, user.nickname, user.avatar, user.phone, user.create_time, user.dept_id as id, user_role.create_time as target_role_create_time " +
            "FROM user " +
			"JOIN user_role ON user.id = user_role.user_id AND user_role.role_id = #{roleId} ${ew.customSqlSegment}")
    List<User> pageByRoleId(Page page, @Param(Constants.WRAPPER) Wrapper wrapper, @Param("roleId") int roleId, DataScope dataScope);


	@Select("SELECT user.* FROM user JOIN user_role ON user.id = user_role.user_id AND user_role.role_id = #{roleId} ${ew.customSqlSegment}")
	List<User> listByRoleId(@Param("roleId") int roleId,  @Param(Constants.WRAPPER)Wrapper wrapper);


	@Select("SELECT * FROM user where phone = #{phone}")
	User getPhoneIsExist(@Param("phone") String phone);

	@Update("UPDATE `user` SET `phone` = #{user.phone}, `password` = #{user.password}" +
			", `salt` = #{user.salt}, `ma_open_id` = #{user.maOpenId}, `mp_open_id` = #{user.mpOpenId}, `union_id` = #{user.unionId}, " +
			"`nickname` = #{user.nickname}, `gender` = #{user.gender}, `language` = #{user.language}, `city` = #{user.city}, `province` = #{user.province}, " +
			"`country` = #{user.country}, `avatar` = #{user.avatar}, `is_subscribe` = #{user.isSubscribe}, `dept_id` = #{user.deptId}, " +
			"`lock_flag` = #{user.lockFlag}, " +
			"`del_flag` = #{user.delFlag}, `inviter_id` = #{user.inviterId}, `real_name` = #{user.realName}, `remarks` = #{user.remarks}, " +
			"`confirm_order` = #{user.confirmOrder}, `is_im_state` = #{user.isImState} WHERE `id` = #{user.id} ")
    int updateUserById(@Param("user")User user);

	@Select("SELECT * FROM user where mp_open_id = #{mpOpenId}")
	User getMpOpenIdIsExist(@Param("mpOpenId")String mpOpenId);

	@Select("SELECT * FROM user where ma_open_id = #{maOpenId}")
	User getMaOpenIdIsExist(@Param("maOpenId")String maOpenId);

	@Select("SELECT * FROM user where union_id = #{unionId}")
	User getUnionIdIsExist(@Param("unionId")String unionId);

	@Update(" UPDATE `user` SET phone= null WHERE  id = #{userId}  AND del_flag = 1")
	int updateUserIsDelSetPhoneNull(@Param("userId")Integer userId);
}
