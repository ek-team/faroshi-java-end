package cn.cuptec.faros.config.com;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "com")
@Data
public class Url {
    private String url;
    private String payUrl;
    private String chatServer;
    private String refundUrl;
}
