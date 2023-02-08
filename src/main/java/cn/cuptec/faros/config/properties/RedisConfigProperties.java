package cn.cuptec.faros.config.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "redis")
public class RedisConfigProperties {

    private String host="119.37.193.94";
    private String port="6379";
    private String password="Jiuhao@_2017";
    private String database="3";

}