package cn.cuptec.faros.config.wx;

import com.alibaba.ttl.TransmittableThreadLocal;
import lombok.experimental.UtilityClass;

/**
 * 微信上下文工具类
 */
@UtilityClass
public class WxMpContextHolder {

	private final ThreadLocal<String> THREAD_LOCAL_APPID = new TransmittableThreadLocal<>();


	/**
	 * TTL 设置appId
	 *
	 * @param appId
	 */
	public void setAppId(String appId) {
		THREAD_LOCAL_APPID.set(appId);
	}

	/**
	 * 获取TTL中的appId
	 *
	 * @return
	 */
	public String getAppId() {
		return THREAD_LOCAL_APPID.get();
	}

	public void clear() {
		THREAD_LOCAL_APPID.remove();
	}
}
