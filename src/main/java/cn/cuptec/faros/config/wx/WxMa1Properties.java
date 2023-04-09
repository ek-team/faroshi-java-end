package cn.cuptec.faros.config.wx;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "wx.ma1")
@Data
public class WxMa1Properties {

    private String appId;

    private String secret;

    private String token;

    private String aesKey;

    private String msgFormat;

}

