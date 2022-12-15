package cn.cuptec.faros.service.handler.login;

import cn.cuptec.faros.entity.User;

/**
 * 登录处理器
 */
public interface LoginHandler {

	/***
	 * 数据合法性校验
	 * @param loginStr 通过用户传入获取唯一标识
	 * @return
	 */
	boolean check(String loginStr);

	/**
	 * 通过用户传入获取唯一标识
	 *
	 * @param loginStr
	 * @return
	 */
	String identify(String loginStr);

	/**
	 * 通过openId 获取用户信息
	 *
	 * @param identify
	 * @return
	 */
	User info(String identify);

	/**
	 * 处理方法
	 *
	 * @param loginStr 登录参数
	 * @return
	 */
    User handle(String loginStr);
}
