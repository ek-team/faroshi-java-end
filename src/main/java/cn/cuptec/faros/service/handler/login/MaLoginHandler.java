package cn.cuptec.faros.service.handler.login;

import cn.binarywang.wx.miniapp.api.WxMaUserService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import cn.cuptec.faros.common.constrants.CommonConstants;
import cn.cuptec.faros.config.security.exception.WxNotRegException;
import cn.cuptec.faros.config.wx.WxMaConfiguration;
import cn.cuptec.faros.entity.User;
import cn.cuptec.faros.service.UserService;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 小程序登录
 */
@Slf4j
@Component("MA")
@AllArgsConstructor
public class MaLoginHandler extends AbstractLoginHandler {

    private final UserService userService;

    @Override
    @SneakyThrows
    public boolean check(String loginStr) {
        return true;    //code不需要验证
    }

    @Override
    public String identify(String loginStr) {
        return loginStr; //微信code
    }

    @Override
    @SneakyThrows
    public User info(String identify) {
        log.info("小程序登录:" + identify);
        //根据code获取微信信息
        WxMaUserService wxMaUserService = WxMaConfiguration.getWxMaService().getUserService();
        WxMaJscode2SessionResult sessionInfo = wxMaUserService.getSessionInfo(identify);
        log.info("小程序登录sessionInfo:{}" + sessionInfo.toString());
        User user = userService.getBaseMapper().getUnionIdIsExist(sessionInfo.getUnionid());
        if (user == null) {
            log.info("用户信息为空");
            return null;
        }

        if (StringUtils.isEmpty(user.getMaOpenId())) {
            //若小程序openId为空，更新
            user.setMaOpenId(sessionInfo.getOpenid());
            userService.updateById(user);
        }

        User user1 = userService.refactByUser(user);
        return user1;
    }
}
