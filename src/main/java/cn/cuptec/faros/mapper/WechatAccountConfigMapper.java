package cn.cuptec.faros.mapper;

import cn.cuptec.faros.entity.UserOrder;
import cn.cuptec.faros.entity.WechatAccountConfig;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * @Description:
 * @Author mby
 * @Date 2021/8/17 10:21
 */
public interface WechatAccountConfigMapper extends BaseMapper<WechatAccountConfig> {

    @Select("SELECT user_id from user_role WHERE user_id in(SELECT id FROM `user` WHERE dept_id =#{depId})   and role_id=17")
    List<Integer> selectByDepId(int depId);
}
