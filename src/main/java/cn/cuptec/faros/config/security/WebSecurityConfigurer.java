package cn.cuptec.faros.config.security;

import cn.cuptec.faros.config.security.handler.MobileLoginFailureHandler;
import cn.cuptec.faros.config.security.handler.MobileLoginSuccessHandler;
import cn.cuptec.faros.config.security.service.CustomUserDetailsService;
import cn.cuptec.faros.config.security.social.SocialSecurityConfigurer;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.provider.ClientDetailsService;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * 认证相关配置
 */
@Configuration
@Order(SecurityProperties.BASIC_AUTH_ORDER)
//@EnableWebSecurity(debug = true)
@EnableGlobalMethodSecurity(prePostEnabled=true)    //开启接口权限拦截
public class WebSecurityConfigurer extends WebSecurityConfigurerAdapter {


	@Bean
	@Override
	@SneakyThrows
	public AuthenticationManager authenticationManagerBean() {
		return super.authenticationManagerBean();
	}

//	@Bean
//	public CorsConfigurationSource corsConfigurationSource(){
//        CorsConfiguration corsConfiguration = new CorsConfiguration();
//        corsConfiguration.setAllowedOrigins(Arrays.asList("*"));
//        corsConfiguration.setAllowedMethods(Arrays.asList("GET", "PUT", "DELETE", "POST", "OPTIONS"));
//        corsConfiguration.setAllowCredentials(false);
//        corsConfiguration.setAllowedHeaders(Arrays.asList("*"));
//        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//        source.registerCorsConfiguration("/**", corsConfiguration);
//        return source;
//    }

//    /**
//     * 不拦截静态资源
//     *
//     * @param web
//     */
//    @Override
//    public void configure(WebSecurity web) {
//        web.ignoring().antMatchers(
//                "/css/**",
//                "**.css",
//                "/js/**",
//                "/**.js",
//                "**.html",
//                "/instances",
//                "/actuator/**",
//                "/assets/**",
//                "/applications",
//                "/portal/**",
//                "/social/token",
//                "/city",
//                "/file/**",
//                "/city/**",
//                "/liveQrCode/dispatcher/**"
//
//        );
//    }
}
