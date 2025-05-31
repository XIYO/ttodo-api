package point.zzicback.common.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import point.zzicback.auth.config.properties.JwtProperties;

@Configuration
@EnableConfigurationProperties(JwtProperties.class)
public class AppConfig {}
