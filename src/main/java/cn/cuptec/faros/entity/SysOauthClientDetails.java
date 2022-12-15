package cn.cuptec.faros.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.extension.activerecord.Model;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
@ApiModel(value = "客户端信息")
public class SysOauthClientDetails extends Model<SysOauthClientDetails> {

	private static final long serialVersionUID = 1L;

	@ApiModelProperty(value = "客户端ID", required = true)
	@NotBlank(message = "client_id 不能为空")
	@TableId(value = "client_id", type = IdType.INPUT)
	private String clientId;

	@ApiModelProperty(value = "客户端密钥", required = true)
	@NotBlank(message = "client_secret 不能为空")
	private String clientSecret;

	@ApiModelProperty(value = "资源ID")
	private String resourceIds;

	@ApiModelProperty(value = "作用域", required = true)
	@NotBlank(message = "scope 不能为空")
	private String scope;

	@ApiModelProperty(value = "授权方式（A,B,C）")
	private String authorizedGrantTypes;

	private String webServerRedirectUri;

	private String authorities;

	@ApiModelProperty(value = "请求令牌有效时间")
	private Integer accessTokenValidity;

	@ApiModelProperty(value = "刷新令牌有效时间")
	private Integer refreshTokenValidity;

	@ApiModelProperty(value = "扩展信息")
	private String additionalInformation;

	@ApiModelProperty(value = "是否自动放行")
	private String autoapprove;

	@Override
	protected Serializable pkVal() {
		return this.clientId;
	}

}
