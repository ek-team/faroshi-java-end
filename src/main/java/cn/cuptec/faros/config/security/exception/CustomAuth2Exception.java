package cn.cuptec.faros.config.security.exception;

import cn.cuptec.faros.config.security.exception.resolver.CustomAuth2ExceptionSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Getter;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;

/**
 * 自定义OAuth2Exception
 */
@JsonSerialize(using = CustomAuth2ExceptionSerializer.class)
public class CustomAuth2Exception extends OAuth2Exception {
	@Getter
	private String errorCode;

	public CustomAuth2Exception(String msg) {
		super(msg);
	}

	public CustomAuth2Exception(String msg, String errorCode) {
		super(msg);
		this.errorCode = errorCode;
	}
}
