package cn.cuptec.faros.config.security.social;

import cn.cuptec.faros.config.security.exception.resolver.ResourceAuthExceptionEntryPoint;
import cn.cuptec.faros.config.security.service.CustomUserDetailsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.stereotype.Component;

/**
 * 手机号登录配置入口
 */
@Getter
@Setter
//@Component
public class SocialSecurityConfigurer extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {

	@Autowired
	private AuthenticationEventPublisher defaultAuthenticationEventPublisher;
	private AuthenticationSuccessHandler mobileLoginSuccessHandler;
	private AuthenticationFailureHandler mobileLoginFailureHandler;
	private CustomUserDetailsService userDetailsService;

	@Override
	public void configure(HttpSecurity http) {
		SocialAuthenticationFilter socialAuthenticationFilter = new SocialAuthenticationFilter();
		socialAuthenticationFilter.setAuthenticationManager(http.getSharedObject(AuthenticationManager.class));
		socialAuthenticationFilter.setAuthenticationSuccessHandler(mobileLoginSuccessHandler);
		socialAuthenticationFilter.setAuthenticationFailureHandler(mobileLoginFailureHandler);
		socialAuthenticationFilter.setEventPublisher(defaultAuthenticationEventPublisher);
		socialAuthenticationFilter.setAuthenticationEntryPoint(new ResourceAuthExceptionEntryPoint());

		SocialAuthenticationProvider socialAuthenticationProvider = new SocialAuthenticationProvider();
		socialAuthenticationProvider.setUserDetailsService(userDetailsService);
		http.authenticationProvider(socialAuthenticationProvider)
			.addFilterAfter(socialAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
	}
}
