package cn.cuptec.faros.config.security.social;

import cn.cuptec.faros.config.security.util.CustomPreAuthenticationChecks;
import cn.cuptec.faros.config.security.service.CustomUserDetailsService;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsChecker;

/**
 * 手机登录校验逻辑
 * 验证码登录、社交登录
 */
@Slf4j
public class SocialAuthenticationProvider implements AuthenticationProvider {
	private MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();
	private UserDetailsChecker detailsChecker = new CustomPreAuthenticationChecks();

	@Getter
	@Setter
	private CustomUserDetailsService userDetailsService;

	@Override
	@SneakyThrows
	public Authentication authenticate(Authentication authentication) {
		SocialAuthenticationToken socialAuthenticationToken = (SocialAuthenticationToken) authentication;

		String principal = socialAuthenticationToken.getPrincipal().toString();
		UserDetails userDetails = userDetailsService.loadUserBySocial(principal);
		if (userDetails == null) {
			log.info("构建用户为空");

			throw new BadCredentialsException(messages.getMessage(
					"AbstractUserDetailsAuthenticationProvider.noopBindAccount",
					"构建用户为空"));
		}

		// 检查账号状态
		detailsChecker.check(userDetails);

		SocialAuthenticationToken authenticationToken = new SocialAuthenticationToken(userDetails, userDetails.getAuthorities());
		authenticationToken.setDetails(socialAuthenticationToken.getDetails());
		return authenticationToken;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return SocialAuthenticationToken.class.isAssignableFrom(authentication);
	}
}
