package cn.cuptec.faros.service.handler.login;

import cn.binarywang.wx.miniapp.bean.WxMaUserInfo;
import cn.cuptec.faros.common.utils.WxMaUtil;
import cn.cuptec.faros.config.wx.WxMaConfiguration;
import cn.cuptec.faros.entity.User;
import cn.cuptec.faros.service.UserService;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

/**
 * 小程序注册登录处理器
 * 参数形式： RMA@sessionKey:encryptedData:iv:unionId:maOpenId
 */
@Component("RMA")
@AllArgsConstructor
@Slf4j
public class MaRegHandler extends AbstractLoginHandler{

    private final UserService userService;

    @Override
    public boolean check(String loginStr) {
        String[] params = loginStr.split(StringPool.COLON);
        if (params.length == 5){
            return true;
        }
        return false;
    }

    @Override
    public String identify(String loginStr) {
        return loginStr;
    }

    @Override
    public User info(String identify) {
        String[] params = identify.split(StringPool.COLON);
        String sessionKey = params[0];
        String encryptedData = params[1];
        String iv = params[2];
        String unionId = params[3];
        String maOpenId = params[4];;

        String phone = WxMaConfiguration.getWxMaService().getUserService().getPhoneNoInfo(sessionKey, encryptedData, iv).getPhoneNumber();
        userService.saveUserOrUpdateMpOpenIdOnDuplicateUnionId(phone, unionId, maOpenId, null);

        User user = userService.getOne(Wrappers.<User>lambdaQuery()
                .eq(User::getPhone, phone)
        );
        return userService.refactByUser(user);
    }
}
