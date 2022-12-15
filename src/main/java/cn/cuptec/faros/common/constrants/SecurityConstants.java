package cn.cuptec.faros.common.constrants;

public interface SecurityConstants {
	/**
	 * 刷新
	 */
	String REFRESH_TOKEN = "refresh_token";
	/**
	 * 验证码有效期
	 */
	int CODE_TIME = 180;
	/**
	 * 验证码长度
	 */
	String CODE_SIZE = "4";
	/**
	 * 角色前缀
	 */
	String ROLE = "ROLE_";
	/**
	 * 前缀
	 */
	String CUP_PREFIX = "cup_";

	/**
	 * oauth 相关前缀
	 */
	String OAUTH_PREFIX = "oauth:";
	/**
	 * 项目的license
	 */
	String AUTHOR = "made by cup";

	/**
	 * 内部
	 */
	String FROM_IN = "Y";

	/**
	 * 标志
	 */
	String FROM = "from";

	/**
	 * OAUTH URL
	 */
	String OAUTH_TOKEN_URL = "/oauth/token";

	/**
	 * 自定义登录URL
	 */
	String SOCIAL_TOKEN_URL = "/social/token";

	/**
	 * {bcrypt} 加密的特征码
	 */
	String BCRYPT = "{bcrypt}";
	/**
	 * sys_oauth_client_details 表的字段，不包括client_id、client_secret
	 */
	String CLIENT_FIELDS = "client_id, CONCAT('{noop}',client_secret) as client_secret, resource_ids, scope, "
			+ "authorized_grant_types, web_server_redirect_uri, authorities, access_token_validity, "
			+ "refresh_token_validity, additional_information, autoapprove";

	/**
	 * JdbcClientDetailsService 查询语句
	 */
	String BASE_FIND_STATEMENT = "select " + CLIENT_FIELDS
			+ " from sys_oauth_client_details";

	/**
	 * 默认的查询语句
	 */
	String DEFAULT_FIND_STATEMENT = BASE_FIND_STATEMENT + " order by client_id";

	/**
	 * 按条件client_id 查询
	 */
	String DEFAULT_SELECT_STATEMENT = BASE_FIND_STATEMENT + " where client_id = ?";

	/**
	 * 资源服务器默认bean名称
	 */
	String RESOURCE_SERVER_CONFIGURER = "resourceServerConfigurerAdapter";

	/**
	 * 客户端模式
	 */
	String CLIENT_CREDENTIALS = "client_credentials";

	/**
	 * 用户ID字段
	 */
	String DETAILS_USER_ID = "user_id";

	/**
	 * 用户名字段
	 */
	String DETAILS_USERNAME = "username";

	/**
	 * 用户部门字段
	 */
	String DETAILS_DEPT_ID = "dept_id";

    /**
     * 用户昵称字段
     */
    String DETAILS_NICKNAME = "nickname";

    /**
     * 用户头像字段
     */
    String DETAILS_AVATAR = "avatar";

	/**
	 * 租户ID 字段
	 */
	String DETAILS_TENANT_ID = "tenant_id";

	/**
	 * 协议字段
	 */
	String DETAILS_LICENSE = "license";

	/**
	 * AES 加密
	 */
	String AES = "aes";

	String CODE = "code";

	String APP_ID = "appId";

	String APP_SECRET = "secret";

	/**
	 * 微信程序类型  小程序或者微信公众号
	 */
	String MICRO_APP_AUTH_TYPE = "microAppAuthType";

	String MICRO_APP_AUTH_TYPE_MA = "ma";

	String MICRO_APP_AUTH_TYPE_MP = "mp";

	String MICRO_APP_AUTH_TYPE_OPEN = "open";

	String PHONE = "phone";
}
