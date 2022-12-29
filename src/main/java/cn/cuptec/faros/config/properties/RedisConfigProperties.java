package cn.cuptec.faros.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "redis")
public class RedisConfigProperties {

    private String host;
    private String port;
    private String password;
    private String database;

}