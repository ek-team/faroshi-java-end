package cn.cuptec.faros.config.wx;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Configuration
@ConfigurationProperties(prefix = "wx.mp")
@Data
public class WxMpProperties {

    private String appId;

    private String secret;

    private String token;

    private String aesKey;

    private String authRedirectUrl;

}
