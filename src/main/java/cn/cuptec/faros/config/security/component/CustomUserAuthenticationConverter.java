//package cn.cuptec.avr.config.security.component;
//
//import cn.cuptec.faros.config.security.service.CustomUser;
//import cn.cuptec.cup.common.core.constant.SecurityConstants;
//import org.springframework.beans.BeansException;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.ApplicationContextAware;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.AuthorityUtils;
//import org.springframework.security.oauth2.provider.token.UserAuthenticationConverter;
//import org.springframework.stereotype.Component;
//import org.springframework.util.StringUtils;
//
//import java.util.ArrayList;
//import java.util.Collection;
//import java.util.LinkedHashMap;
//import java.util.Map;
//
///**
// * 根据checktoken 的结果转化用户信息
// */
//@Component
//public class CustomUserAuthenticationConverter implements UserAuthenticationConverter, ApplicationContextAware {
//	private static final String N_A = "N/A";
//    @Autowired
//    private ApplicationContext applicationContext;
//	/**
//	 * Extract information about the user to be used in an access token (i.e. for resource servers).
//	 *
//	 * @param authentication an authentication representing a user
//	 * @return a map of key values representing the unique information about the user
//	 */
//	@Override
//	public Map<String, ?> convertUserAuthentication(Authentication authentication) {
//		Map<String, Object> response = new LinkedHashMap<>();
//		response.put(USERNAME, authentication.getName());
//		if (authentication.getAuthorities() != null && !authentication.getAuthorities().isEmpty()) {
//			response.put(AUTHORITIES, AuthorityUtils.authorityListToSet(authentication.getAuthorities()));
//		}
//		return response;
//	}
//
//	/**
//	 * Inverse of {@link #convertUserAuthentication(Authentication)}. Extracts an Authentication from a map.
//	 *
//	 * @param map a map of user information
//	 * @return an Authentication representing the user or null if there is none
//	 */
//	@Override
//	public Authentication extractAuthentication(Map<String, ?> map) {
//		if (map.containsKey(USERNAME)) {
//			Collection<? extends GrantedAuthority> authorities = getAuthorities(map);
//
//			String username = (String) map.get(USERNAME);
//			Integer id = (Integer) map.get(SecurityConstants.DETAILS_USER_ID);
//			Integer id = (Integer) map.get(SecurityConstants.DETAILS_DEPT_ID);
//            String nickname = (String) map.get(SecurityConstants.DETAILS_NICKNAME);
//            String avatar = (String) map.get(SecurityConstants.DETAILS_AVATAR);
//			CustomUser user = new CustomUser(id, id, nickname, avatar, username, N_A, true
//					, true, true, true, authorities);
//			return new UsernamePasswordAuthenticationToken(user, N_A, authorities);
//		}
//		return null;
//	}
//
//	private Collection<? extends GrantedAuthority> getAuthorities(Map<String, ?> map) {
//		Object authorities = map.get(AUTHORITIES);
//		if (authorities instanceof String) {
//			return AuthorityUtils.commaSeparatedStringToAuthorityList((String) authorities);
//		}
//		if (authorities instanceof Collection) {
//			return AuthorityUtils.commaSeparatedStringToAuthorityList(StringUtils
//					.collectionToCommaDelimitedString((Collection<?>) authorities));
//		}
//		return new ArrayList<>();
//	}
//
//    @Override
//    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
//        this.applicationContext = applicationContext;
//    }
//}
