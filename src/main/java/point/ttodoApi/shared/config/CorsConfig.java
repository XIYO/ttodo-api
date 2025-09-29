package point.ttodoApi.shared.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.web.cors.*;
import point.ttodoApi.shared.config.properties.CorsProperties;

@Configuration
@RequiredArgsConstructor
public class CorsConfig {

  private final CorsProperties corsProperties;

  @Bean
  public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowCredentials(corsProperties.isAllowCredentials());

    if (corsProperties.getAllowedOrigins() != null) {
      corsProperties.getAllowedOrigins().forEach(config::addAllowedOrigin);
    }

    if (corsProperties.getAllowedHeaders() != null) {
      corsProperties.getAllowedHeaders().forEach(config::addAllowedHeader);
    }

    if (corsProperties.getAllowedMethods() != null) {
      corsProperties.getAllowedMethods().forEach(config::addAllowedMethod);
    }

    config.setMaxAge(corsProperties.getMaxAge());

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
  }
}
