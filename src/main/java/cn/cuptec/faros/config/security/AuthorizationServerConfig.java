package cn.cuptec.faros.config.security;

import cn.cuptec.faros.common.constrants.SecurityConstants;
import cn.cuptec.faros.config.security.exception.resolver.CustomWebResponseExceptionTranslator;
import cn.cuptec.faros.config.security.service.CustomClientDetailsService;
import cn.cuptec.faros.config.security.service.CustomUser;
import cn.cuptec.faros.config.security.service.CustomUserDetailsService;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.*;
import org.springframework.security.oauth2.provider.token.store.redis.RedisTokenStore;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 认证授权服务器配置
 */
@Configuration
@AllArgsConstructor
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {
	private final DataSource dataSource;
	private final CustomUserDetailsService customUserDetailsService;
	private final AuthenticationManager authenticationManagerBean;
	private final RedisConnectionFactory redisConnectionFactory;
	private PasswordEncoder passwordEncoder;
    /**
     * 客户端信息配置
     * @param clients
     */
	@Override
	@SneakyThrows
	public void configure(ClientDetailsServiceConfigurer clients) {
		clients.inMemory()
				.withClient("test")
				.secret(this.passwordEncoder.encode("test"))
				// 为了测试，所以开启所有的方式，实际业务根据需要选择
				.authorizedGrantTypes("authorization_code", "password", "client_credentials", "implicit", "refresh_token")
				.accessTokenValiditySeconds(15552000)
				.refreshTokenValiditySeconds(864000)
				.scopes("select")
				// false跳转到授权页面，在授权码模式中会使用到
				.autoApprove(false)
				// 验证回调地址
				.redirectUris("http://www.baidu.com");
//		CustomClientDetailsService clientDetailsService = new CustomClientDetailsService(dataSource);
//		clientDetailsService.setSelectClientDetailsSql(SecurityConstants.DEFAULT_SELECT_STATEMENT);
//		clientDetailsService.setFindClientDetailsSql(SecurityConstants.DEFAULT_FIND_STATEMENT);
//		clients.withClientDetails(clientDetailsService);
	}

	@Override
	public void configure(AuthorizationServerSecurityConfigurer oauthServer) {
	    // 允许表单验证
		oauthServer
				.allowFormAuthenticationForClients()
				.checkTokenAccess("isAuthenticated()")
                .checkTokenAccess("permitAll()");
	}

    /**
     * token及用户信息配置存储到redis
     * @param endpoints
     */
	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) {
		endpoints
				.allowedTokenEndpointRequestMethods(HttpMethod.GET, HttpMethod.POST)
				.tokenStore(tokenStore())
				.tokenEnhancer(tokenEnhancer())
				.userDetailsService(customUserDetailsService)
				.authenticationManager(authenticationManagerBean)
				.reuseRefreshTokens(false)
				.pathMapping("/oauth/confirm_access", "/token/confirm_access")
				.exceptionTranslator(new CustomWebResponseExceptionTranslator());
	}

	@Bean
	public TokenStore tokenStore() {
		RedisTokenStore tokenStore = new RedisTokenStore(redisConnectionFactory);
		tokenStore.setPrefix(SecurityConstants.CUP_PREFIX + SecurityConstants.OAUTH_PREFIX);
		tokenStore.setAuthenticationKeyGenerator(new DefaultAuthenticationKeyGenerator() {
			@Override
			public String extractKey(OAuth2Authentication authentication) {
				return super.extractKey(authentication);
			}
		});
		return tokenStore;
	}

	/**
	 * token增强，客户端模式不增强。
	 *
	 * @return TokenEnhancer
	 */
	@Bean
	public TokenEnhancer tokenEnhancer() {
		return (accessToken, authentication) -> {
			if (SecurityConstants.CLIENT_CREDENTIALS
					.equals(authentication.getOAuth2Request().getGrantType())) {
				return accessToken;
			}

			final Map<String, Object> additionalInfo = new HashMap<>(8);
			CustomUser customUser = (CustomUser) authentication.getUserAuthentication().getPrincipal();
			additionalInfo.put(SecurityConstants.DETAILS_USER_ID, customUser.getId());
			additionalInfo.put(SecurityConstants.DETAILS_USERNAME, customUser.getUsername());
			additionalInfo.put(SecurityConstants.DETAILS_DEPT_ID, customUser.getDeptId());
            additionalInfo.put(SecurityConstants.DETAILS_NICKNAME, customUser.getNickname());
            additionalInfo.put(SecurityConstants.DETAILS_AVATAR, customUser.getAvatar());
            additionalInfo.put(SecurityConstants.DETAILS_LICENSE, SecurityConstants.AUTHOR);
			((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(additionalInfo);
			return accessToken;
		};
	}

//	@Bean
//	public CustomUserAuthenticationConverter customUserAuthenticationConverter(){
//	    return new CustomUserAuthenticationConverter();
//    }

}
