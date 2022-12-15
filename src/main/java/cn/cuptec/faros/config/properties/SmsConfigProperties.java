package cn.cuptec.faros.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 短信服务参数
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "cup.sms")
public class SmsConfigProperties {

    private String accessKeyId;

    private String accessKeySecret;

    /**
     * 短信签名
     */
    private String signName;

    /**
     * 登录验证码的短信模板
     */
    private String loginTemplate;

    /**
     * 短信验证码有效期，单位秒
     */
    private Integer expireTime;

}
