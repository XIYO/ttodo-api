package point.ttodoApi.shared.config.shared;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import point.ttodoApi.shared.config.auth.properties.JwtProperties;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class AppConfig {
}
