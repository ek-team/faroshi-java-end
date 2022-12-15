package cn.cuptec.faros.service.handler.login;

import cn.cuptec.faros.common.exception.BizException;
import cn.cuptec.faros.entity.User;
import cn.cuptec.faros.service.MobileService;
import cn.cuptec.faros.service.UserService;
import com.baomidou.mybatisplus.core.toolkit.StringPool;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Slf4j
@Component("SMS")
@AllArgsConstructor
public class SmsLoginHandler extends AbstractLoginHandler {
	private final UserService userService;
	private final MobileService mobileService;

	@Override
	public boolean check(String loginStr) {
		String[] loginParameter = loginStr.split(StringPool.COLON);
		if (loginParameter.length != 2)
			throw new BizException(10000, new Object[]{loginStr});
        boolean accessed = mobileService.checkLoginSmsCode(loginParameter[0], loginParameter[1]);
        if (!accessed){
            throw new BadCredentialsException("手机验证码错误");
        }
        return accessed;
    }

	/**
	 * 验证码登录传入为手机号
	 * @param loginStr
	 * @return
	 */
	@Override
	public String identify(String loginStr) {
		return loginStr.split(StringPool.COLON)[0];
	}

	/**
	 * 通过mobile 获取用户信息
	 *
	 * @param identify
	 * @return
	 */
	@Override
	public User info(String identify) {
		User user = userService
				.getOne(Wrappers.<User>lambdaQuery()
                        .eq(User::getPhone, identify)
				);


		if (user == null) {
//			log.info("手机号未注册:{}", identify);
//			return null;
            user = new User();
            user.setPhone(identify);
            user.setNickname("小海螺");
            userService.save(user);
            return user;
		}
		return userService.refactByUser(user);
	}
}
