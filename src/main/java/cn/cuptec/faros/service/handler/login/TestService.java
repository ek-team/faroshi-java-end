package cn.cuptec.faros.service.handler.login;

import cn.cuptec.faros.common.constrants.CommonConstants;
import cn.cuptec.faros.config.wx.WxMpConfiguration;
import cn.cuptec.faros.entity.User;
import cn.cuptec.faros.service.UserRoleService;
import cn.cuptec.faros.service.UserService;
import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.mp.api.WxMpService;
import me.chanjar.weixin.mp.bean.result.WxMpOAuth2AccessToken;
import me.chanjar.weixin.mp.bean.result.WxMpUser;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Service
public class TestService {

    @Resource
    private UserService userService;
    @Resource
    private UserRoleService userRoleService;

    public User info() throws WxErrorException {
        //根据code获取微信信息

        WxMpUser wxMpUser = new WxMpUser();
        wxMpUser.setOpenId("oid41064V_YQ6j14QGAoJdkmirbA");
        wxMpUser.setUnionId("ofX4C1SfVxEF79Y21JtVDPXfv53Y");
        if (wxMpUser != null){
            User user =  userService.getBaseMapper().getMpOpenIdIsExist(wxMpUser.getOpenId());
            if (user == null || !"0".equals(user.getDelFlag())) {
                User u = new User();
                u .setPhone(wxMpUser.getOpenId());
                u .setMpOpenId(wxMpUser.getOpenId());
                u .setUnionId(wxMpUser.getUnionId());
                u .setNickname(wxMpUser.getNickname());
                u .setAvatar(wxMpUser.getHeadImgUrl());
                u .setGender(wxMpUser.getSexDesc());
                u .setProvince(wxMpUser.getProvince());
                u .setCity(wxMpUser.getCity());
                u .setLanguage(wxMpUser.getLanguage());
                u .setIsSubscribe(false);
                u .setLockFlag(CommonConstants.STATUS_NORMAL);
                if (user == null)userService.save(u);
                else {
                    u.setDelFlag(CommonConstants.STATUS_NORMAL);
                    u.setLockFlag(CommonConstants.STATUS_NORMAL);
                    userRoleService.deleteByUserId(user.getId());
                    u.setId(user.getId());
                    userService.getBaseMapper().updateUserById(u);
                }
            }

            if (StringUtils.isEmpty(user.getMpOpenId())){
                //若公众号openId为空，更新
                user.setMpOpenId(wxMpUser.getOpenId());
                userService.updateById(user);
            }
            return userService.refactByUser(user);
        }
        return null;
    }

}
