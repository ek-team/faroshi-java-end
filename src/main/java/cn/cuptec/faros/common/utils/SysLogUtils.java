package cn.cuptec.faros.common.utils;

import cn.cuptec.faros.common.constrants.CommonConstants;
import cn.cuptec.faros.entity.Log;
import cn.hutool.core.util.URLUtil;
import cn.hutool.extra.servlet.ServletUtil;
import cn.hutool.http.HttpUtil;
import lombok.experimental.UtilityClass;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Objects;

/**
 * 系统日志工具类
 */
@UtilityClass
public class SysLogUtils {
	public Log getSysLog() {
		HttpServletRequest request = ((ServletRequestAttributes) Objects
				.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        Log log = new Log();
		log.setCreateBy(Objects.requireNonNull(getUsername()));
		log.setType(CommonConstants.STATUS_NORMAL);
		log.setRemoteAddr(ServletUtil.getClientIP(request));
		log.setRequestUri(URLUtil.getPath(request.getRequestURI()));
		log.setMethod(request.getMethod());
		log.setUserAgent(request.getHeader("user-agent"));
		log.setParams(HttpUtil.toParams(request.getParameterMap()));
		log.setClientId(getClientId());
		return log;
	}

	/**
	 * 获取客户端
	 *
	 * @return clientId
	 */
	private String getClientId() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication instanceof OAuth2Authentication) {
			OAuth2Authentication auth2Authentication = (OAuth2Authentication) authentication;
			return auth2Authentication.getOAuth2Request().getClientId();
		}
		return null;
	}

	/**
	 * 获取用户名称
	 *
	 * @return username
	 */
	private String getUsername() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		if (authentication == null) {
			return null;
		}
		return authentication.getName();
	}

}
