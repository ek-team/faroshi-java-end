package cn.cuptec.faros.common.constrants;

/**
 *
 */
public interface CommonConstants {

	String[] PIC_TYPES = new String[]{"bmp", "dib", "gif", "jfif", "jpe", "jpeg", "jpg", "png", "tif", "tiff", "ico"};

	/**
	 * header 中租户ID
	 */
	String TENANT_ID = "TENANT-ID";

	/**
	 * header 中版本信息
	 */
	String VERSION = "VERSION";

	/**
	 * 租户ID
	 */
	Integer TENANT_ID_1 = 0;

	/**
	 * 删除
	 */
	String STATUS_DEL = "1";
	/**
	 * 正常
	 */
	String STATUS_NORMAL = "0";

	/**
	 * 锁定
	 */
	String STATUS_LOCK = "9";

	/**
	 * 菜单
	 */
	String MENU = "0";

	/**
	 * 菜单树根节点
	 */
	Integer TREE_ROOT_ID = -1;

	/**
	 * 编码
	 */
	String UTF8 = "UTF-8";

	/**
	 * 验证码前缀
	 */
	String IMAGE_CODE_KEY = "DEFAULT_CODE_KEY_";

	/**
	 * 公共参数
	 */
	String PIG_PUBLIC_PARAM_KEY = "PIG_PUBLIC_PARAM_KEY";

	/**
	 * 成功标记
	 */
	Integer SUCCESS = 0;
	/**
	 * 失败标记
	 */
	Integer FAIL = 1;

	String FOLDER_SEPARATOR = "/";

	String VALUE_SEPARATOR = ";";

	String CONCATENATION_CHARACTER = "-";

	/**
	 * 当前记录起始索引
	 */
	String PAGE_NUM = "pageNum";

	/**
	 * 每页显示记录数
	 */
	String PAGE_SIZE = "pageSize";

	/**
	 * 排序列
	 */
	String ORDER_BY_COLUMN = "orderByColumn";

	/**
	 * 排序的方向 "desc" 或者 "asc".
	 */
	String IS_ASC = "isAsc";

	String ASCS = "ascs";

	String DESCS = "descs";

	String SYSTEM = "系统";

	String POINT = ".";

	String COMMA = ",";

	String PIC_BASE64_HEADER_PNG = "data:image/png;base64,";

	String PIC_BASE64_HEADER_JPEG = "data:image/jpeg;base64,";

	String HTTPS = "https";
	String OK = "ok";
	String COLON = ":";
	String DEFAULT_NAME = "默认";

    /**
     * 审批流程结果
     * 具体值对应数据字典 APPROVE_RESULT : PASS-审批通过 REFUSED-审批拒绝 NOT_END-审批中
     */
	String APPROVE_RESULT = "APPROVE_RESULT";

	String APPROVE_RESULT_NOT_END = "NOT_END";
	String APPROVE_RESULT_PASS = "PASS";
	String APPROVE_RESULT_REFUSED = "REFUSED";




    /**
     * 任务审批结果
     * 具体值对应数据字典 APPROVE_OPER : OPER_PASS-审批通过 OPER_REFUSED-审批拒绝 OPER_WAITING-等待处理中
     */


	String APPROVE_OPER = "APPROVE_OPER";

	String APPROVE_OPER_PASS = "APPROVE_OPER_PASS";
	String APPROVE_OPER_PASS_REFUSED = "APPROVE_OPER_REFUSED";
    String APPROVE_OPER_HANDLING = "APPROVE_OPER_HANDLING"; //正在审批
    String APPROVE_OPER_WAITING = "APPROVE_OPER_WAITING"; //等待处理
    String CODE = "code";
}
