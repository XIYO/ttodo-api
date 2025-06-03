package point.zzicback.auth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.*;
import point.zzicback.auth.infrastructure.jwt.CustomJwtAuthConverter;
import point.zzicback.auth.security.resolver.MultiBearerTokenResolver;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
  private final MultiBearerTokenResolver multiBearerTokenResolver;
  private final CustomJwtAuthConverter customJwtAuthConverter;
  private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.csrf(AbstractHttpConfigurer::disable).formLogin(AbstractHttpConfigurer::disable).httpBasic(AbstractHttpConfigurer::disable).sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).authorizeHttpRequests(authorize -> authorize.requestMatchers("/", "/auth/sign-up", "/auth/sign-in", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html", "/actuator/**").permitAll().anyRequest().authenticated()).oauth2ResourceServer(oauth2 -> oauth2.bearerTokenResolver(multiBearerTokenResolver).jwt(jwt -> jwt.jwtAuthenticationConverter(customJwtAuthConverter)).authenticationEntryPoint(jwtAuthenticationEntryPoint));
    return http.build();
  }
}
