package cn.cuptec.faros.service.handler.login;


import cn.cuptec.faros.entity.User;

public abstract class AbstractLoginHandler implements LoginHandler {

	/**
	 * 处理方法
	 *
	 * @param loginStr 登录参数
	 * @return
	 */
	@Override
	public User handle(String loginStr) {
		if (!check(loginStr)) {
			return null;
		}

		String identify = identify(loginStr);
		return info(identify);
	}
}
