package cn.cuptec.faros.config.web;

import cn.cuptec.faros.common.RestResponse;
import cn.cuptec.faros.common.exception.BizException;
import cn.cuptec.faros.common.exception.InnerException;
import cn.cuptec.faros.common.exception.OuterException;
import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

/**
 * 全局异常处理器
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandlerResolver {

	@ExceptionHandler(BizException.class)
	@ResponseStatus(HttpStatus.OK)
	public RestResponse handleBizException(BizException e) {
		log.warn("业务异常, errorCode={}, args={}, ex={}", e.getErrorCode(), JSON.toJSONString(e.getArgs()), e.getMessage(), e);
		return RestResponse.failed(e.getMessage());
	}

	@ExceptionHandler(InnerException.class)
	@ResponseStatus(HttpStatus.OK)
	public RestResponse handleInnerException(InnerException e) {
		log.warn("内部异常, returnMsg={}, args={}, innerExceptionMsg={}, ex={}", e.getReturnMsg(), JSON.toJSONString(e.getArgs()), e.getInnerException() != null ? e.getInnerException().getMessage() : "", e.getMessage(), e);
		return RestResponse.failed(e.getReturnMsg());
	}

	@ExceptionHandler(OuterException.class)
	@ResponseStatus(HttpStatus.OK)
	public RestResponse handleOuterException(OuterException e) {
		log.warn("外部异常, returnMsg={}, args={}, innerExceptionMsg={}, ex={}", e.getReturnMsg(), JSON.toJSONString(e.getArgs()), e.getOuterException() != null ? e.getOuterException().getMessage() : "", e.getMessage(), e);
		return RestResponse.failed(e.getReturnMsg());
	}

	/**
	 * 全局异常.
	 *
	 * @param e the e
	 * @return RestResponse
	 */
	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.OK)
	public RestResponse handleGlobalException(Exception e) {
		log.error("全局异常信息 ex={}", e.getMessage(), e);
		return RestResponse.failed(e.getLocalizedMessage());
	}

	/**
	 * 全局异常.
	 * @param e the e
	 * @return RestResponse
	 */
	@ExceptionHandler(RuntimeException.class)
	@ResponseStatus(HttpStatus.OK)
	public RestResponse handleRuntimeException(RuntimeException e) {
		log.error("全局异常信息 ex={}", e.getMessage(), e);
        return RestResponse.failed(e.getLocalizedMessage());
	}

	/**
	 * AccessDeniedException
	 *
	 * @param e the e
	 * @return RestResponse
	 */
	@ExceptionHandler(AccessDeniedException.class)
	@ResponseStatus(HttpStatus.FORBIDDEN)
	public RestResponse handleAccessDeniedException(AccessDeniedException e) {
		String msg = SpringSecurityMessageSource.getAccessor()
				.getMessage("AbstractAccessDecisionManager.accessDenied"
						, e.getMessage());
		log.error("拒绝授权异常信息 ex={}", msg, e);
		return RestResponse.failed(e.getLocalizedMessage());
	}

	/**
	 * validation Exception
	 *
	 * @param exception
	 * @return RestResponse
	 */
	@ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public RestResponse handleBodyValidException(MethodArgumentNotValidException exception) {
		List<FieldError> fieldErrors = exception.getBindingResult().getFieldErrors();
		log.error("参数绑定异常,ex = {}", fieldErrors.get(0).getDefaultMessage());
		return RestResponse.failed(fieldErrors.get(0).getDefaultMessage());
	}

	@ExceptionHandler(MissingServletRequestParameterException.class)
	@ResponseStatus(HttpStatus.OK)
	public RestResponse handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
		log.error("缺少参数 ex={}", e.getMessage(), e);
		return RestResponse.failed("缺少参数:" + e.getParameterName());
	}
}
