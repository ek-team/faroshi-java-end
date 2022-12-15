package cn.cuptec.faros.config.security.exception.resolver;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.common.constrants.CommonConstants;
import cn.cuptec.faros.config.security.exception.CustomAuth2Exception;
import cn.cuptec.faros.config.security.exception.WxNotRegException;
import cn.hutool.http.HttpStatus;
import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * 客户端异常处理
 * 1. 可以根据 AuthenticationException 不同细化异常处理
 */
@Slf4j
@Component
@AllArgsConstructor
public class ResourceAuthExceptionEntryPoint implements AuthenticationEntryPoint {

	@Override
	@SneakyThrows
	public void commence(HttpServletRequest request, HttpServletResponse response,
						 AuthenticationException authException) {
		response.setCharacterEncoding(CommonConstants.UTF8);
		response.setContentType(MediaType.APPLICATION_JSON_UTF8_VALUE);
		RestResponse<Object> result = new RestResponse<>();

		if (authException.getCause() instanceof WxNotRegException){
            result.setCode(((WxNotRegException) authException.getCause()).getHttpErrorCode());
            result.setData(((WxNotRegException) authException.getCause()).getPreRegData());
        }else {
            result.setCode(HttpStatus.HTTP_UNAUTHORIZED);
        }

		if (authException != null) {
			if (authException instanceof InsufficientAuthenticationException){
				result.setMsg("");
			}else {
				result.setMsg(authException.getMessage());
			}
		}
		//状态码仍旧给出200，而非401
		response.setStatus(HttpStatus.HTTP_OK);
		PrintWriter printWriter = response.getWriter();
    printWriter.append(JSON.toJSONString(result));
	}
}
