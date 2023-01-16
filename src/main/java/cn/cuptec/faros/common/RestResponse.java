package cn.cuptec.faros.common;

import cn.cuptec.faros.common.constrants.CommonConstants;
import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * 响应信息主体
 */
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class RestResponse<T> implements Serializable {
	private static final long serialVersionUID = 1L;

	@Getter
	@Setter
	//返回标记：成功标记=0，失败标记=1
	private int code;

	@Getter
	@Setter
	private String msg;


	@Getter
	@Setter
	private T data;

	public static <T> RestResponse<T> ok() {
		return restResult(CommonConstants.SUCCESS, null, null);
	}

	public static <T> RestResponse<T> ok(T data) {
		return restResult( CommonConstants.SUCCESS, null, data);
	}

	public static <T> RestResponse<T> ok(String msg, T data) {
		return restResult(CommonConstants.SUCCESS, msg, data);
	}

	public static <T> RestResponse<T> failed() {
		return restResult(CommonConstants.FAIL,null, null);
	}

	public static <T> RestResponse<T> failed(String msg) {
		return restResult(CommonConstants.FAIL, msg, null);
	}

	public static <T> RestResponse<T> failed(String msg ,T data) {
		return restResult(CommonConstants.FAIL, msg, data);
	}

	private static <T> RestResponse<T> restResult(int code, String msg, T data) {
		RestResponse<T> apiResult = new RestResponse<>();
		apiResult.setCode(code);
		apiResult.setData(data);
		apiResult.setMsg(msg);
		return apiResult;
	}

	@JSONField(serialize = false, deserialize = false)
	public boolean isOk(){
		return code == CommonConstants.SUCCESS;
	}
}
