package cn.cuptec.faros.config.security.social;

import cn.cuptec.faros.common.constrants.SecurityConstants;
import cn.cuptec.faros.common.utils.http.GetRequestJsonUtil;
import cn.cuptec.faros.config.security.filter.AbstractAuthenticationFilter;
import com.alibaba.fastjson.JSONObject;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 自定义登录验证filter
 */
public class SocialAuthenticationFilter extends AbstractAuthenticationFilter {
	private static final String SPRING_SECURITY_FORM_SOCIAL_KEY = "social";
	@Getter
	@Setter
	private String socialParameter = SPRING_SECURITY_FORM_SOCIAL_KEY;


	public SocialAuthenticationFilter() {
		super(new AntPathRequestMatcher(SecurityConstants.SOCIAL_TOKEN_URL, "POST"));
	}

	@Override
	@SneakyThrows
	public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response) {
		if (postOnly && !request.getMethod().equals(HttpMethod.POST.name())) {
			throw new AuthenticationServiceException(
					"Authentication method not supported: " + request.getMethod());
		}

		String parameter = obtainMobile(request);

		if (parameter == null) {
			parameter = "";
		}

		parameter = parameter.trim();

		SocialAuthenticationToken socialAuthenticationToken = new SocialAuthenticationToken(parameter);

		setDetails(request, socialAuthenticationToken);

		Authentication authResult = authenticate(socialAuthenticationToken, request, response);

		return authResult;
	}

	@SneakyThrows
	private String obtainMobile(HttpServletRequest request) {

	    String socialParam;

        JSONObject requestJsonObject = GetRequestJsonUtil.getRequestJsonObject(request);
        if (requestJsonObject != null && requestJsonObject.containsKey(SPRING_SECURITY_FORM_SOCIAL_KEY)){
            socialParam = requestJsonObject.getString(SPRING_SECURITY_FORM_SOCIAL_KEY);
        }
        else {
            socialParam = request.getParameter(socialParameter);
        }

        return socialParam;
	}

}

