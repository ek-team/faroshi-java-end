package cn.cuptec.faros.entity;

import cn.cuptec.faros.common.annotation.Queryable;
import cn.cuptec.faros.common.enums.QueryLogical;
import com.alibaba.fastjson.annotation.JSONField;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 租户日志表
 */
@Data
public class Log implements Serializable {

	private static final long serialVersionUID = 1L;

	/**
	 * 编号
	 */
	@TableId(value = "id", type = IdType.AUTO)
	private Long id;
	/**
	 * 日志类型
	 */
	@NotBlank(message = "日志类型不能为空")
	@Queryable(queryLogical = QueryLogical.EQUAL)
	private String type;
	/**
	 * 日志标题
	 */
	@NotBlank(message = "日志标题不能为空")
	private String title;
	/**
	 * 创建者
	 */
	private String createBy;
	/**
	 * 创建时间
	 */
	private LocalDateTime createTime;
	/**
	 * 更新时间
	 */
	private LocalDateTime updateTime;
	/**
	 * 操作IP地址
	 */
	private String remoteAddr;
	/**
	 * 用户代理
	 */
	private String userAgent;
	/**
	 * 请求URI
	 */
	private String requestUri;
	/**
	 * 操作方式
	 */
	private String method;
	/**
	 * 操作提交的数据
	 */
	private String params;
	/**
	 * 执行时间
	 */
	private Long time;

	/**
	 * 异常信息
	 */
	private String exception;

	/**
	 * 服务ID
	 */
	private String clientId;
	/**
	 * 描述
	 */
	@TableField(value = "`describe`")
	private String describe;

	@ApiModelProperty(value = "删除标记", readOnly = true, hidden = true)
	@JSONField(deserialize = false, serialize = false)
	@TableLogic
	private String delFlag;

}
