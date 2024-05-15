package hope.smarteditor.common.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "hope.jwt")
@Data
public class JwtProperties {

    /**
     * jwt令牌相关配置
     */
    private String SecretKey;
    private long Ttl;
    private String TokenName;


}
