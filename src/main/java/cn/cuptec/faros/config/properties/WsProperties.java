package cn.cuptec.faros.config.properties;

import com.baomidou.mybatisplus.core.toolkit.StringPool;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "cup.ws")
public class WsProperties {

    private String host;

    private Integer port;

    private String checkTokenUrl;

    public String getWebSoctetUrl() {
        return "ws://" + host + StringPool.COLON + port + StringPool.SLASH + "ws";
    }

}