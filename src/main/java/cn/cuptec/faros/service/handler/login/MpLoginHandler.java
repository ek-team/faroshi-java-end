package cn.cuptec.faros.service.handler.login;

import cn.binarywang.wx.miniapp.api.WxMaUserService;
import cn.cuptec.faros.common.constrants.CommonConstants;
import cn.cuptec.faros.common.exception.BizException;
import cn.cuptec.faros.config.wx.WxMaConfiguration;
import cn.cuptec.faros.config.wx.WxMpConfiguration;
import cn.cuptec.faros.entity.User;
import cn.cuptec.faros.service.UserRoleService;
import cn.cuptec.faros.service.UserService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.api.WxMpUserService;
import me.chanjar.weixin.mp.bean.result.WxMpOAuth2AccessToken;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import org.springframework.stereotype.Component;

/**
 * 公众号登录
 */
@Slf4j
@Component("MP")
@AllArgsConstructor
public class MpLoginHandler extends AbstractLoginHandler {

    private final UserService userService;
    private final UserRoleService userRoleService;

    @Override
    public boolean check(String loginStr) {
//        String[] loginParameter = loginStr.split(StringPool.COLON);
//        if (loginParameter.length != 2)
//            throw new BizException(10000, new Object[]{loginStr});
        return true;
    }

    @Override
    public String identify(String loginStr) {
//        return loginStr.split(StringPool.AT)[1]; //微信code
        return loginStr;
    }

    @Override
    @SneakyThrows
    public User info(String identify) {
        //根据code获取微信信息

        log.info("公众号登录:" + identify);
        String[] split = identify.split("/");
        WxMpService wxMpService;
        identify = split[0];
        if (split.length == 1) {

            wxMpService = WxMpConfiguration.getWxMpService();
        } else {

            wxMpService = WxMpConfiguration.getWxMp1Service();
        }
        //MA@casjkdhaskjdhaskdj
        WxMpOAuth2AccessToken accessToken = wxMpService.oauth2getAccessToken(identify);
        log.info("公众号获取用户信息accessToken={}", JSON.toJSONString(accessToken));
        WxMpUser wxMpUser = wxMpService.oauth2getUserInfo(accessToken, null);
        log.info("公众号获取用户信息={}", JSON.toJSONString(wxMpUser));
        if (wxMpUser != null) {
            User user = userService.getBaseMapper().getUnionIdIsExist(wxMpUser.getUnionId());
            if (user == null) {
                user = new User();
                user.setPhone(wxMpUser.getOpenId());
                user.setMpOpenId(wxMpUser.getOpenId());
                user.setUnionId(wxMpUser.getUnionId());
                user.setNickname(wxMpUser.getNickname());
                user.setAvatar(wxMpUser.getHeadImgUrl());
                user.setGender(wxMpUser.getSexDesc());
                user.setProvince(wxMpUser.getProvince());
                user.setCity(wxMpUser.getCity());
                user.setLanguage(wxMpUser.getLanguage());
                user.setIsSubscribe(false);
                user.setLockFlag(CommonConstants.STATUS_NORMAL);
                userService.save(user);
            } else if (!"0".equals(user.getDelFlag())) {
                user.setDelFlag(CommonConstants.STATUS_NORMAL);
                user.setLockFlag(CommonConstants.STATUS_NORMAL);
                userRoleService.deleteByUserId(user.getId());
                user.setId(user.getId());
                userService.getBaseMapper().updateUserById(user);
            }

            if (StringUtils.isEmpty(user.getMpOpenId())) {
                //若公众号openId为空，更新
                user.setMpOpenId(wxMpUser.getOpenId());
                userService.updateById(user);
            }

            //若小程序Uniond为空，更新
            user.setUnionId(wxMpUser.getUnionId());
            userService.updateById(user);

            return userService.refactByUser(user);
        }
        return null;
    }
}
