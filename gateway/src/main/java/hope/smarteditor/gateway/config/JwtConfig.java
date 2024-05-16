package hope.smarteditor.gateway.config;

import hope.smarteditor.common.properties.JwtProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtConfig {

    @Bean
    public JwtProperties jwtProperties() {
        return new JwtProperties();
    }
}
