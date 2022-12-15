package cn.cuptec.faros.vo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * 前端日志vo
 */
@Data
@ApiModel
public class PreLogVo {
	@ApiModelProperty(value = "url")
	private String url;
	@ApiModelProperty(value = "时间")
	private String time;
	@ApiModelProperty(value = "用户")
	private String user;
	@ApiModelProperty(value = "类型")
	private String type;
	@ApiModelProperty(value = "日志消息")
	private String message;
	@ApiModelProperty(value = "异常信息")
	private String stack;
	@ApiModelProperty(value = "信息")
	private String info;
}
