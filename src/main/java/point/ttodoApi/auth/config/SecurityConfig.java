package point.ttodoApi.auth.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import point.ttodoApi.auth.infrastructure.jwt.CustomJwtAuthConverter;
import point.ttodoApi.auth.security.resolver.MultiBearerTokenResolver;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {
  private final MultiBearerTokenResolver multiBearerTokenResolver;
  private final CustomJwtAuthConverter customJwtAuthConverter;
  private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

  @Bean
  public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http.cors(Customizer.withDefaults()).csrf(AbstractHttpConfigurer::disable).formLogin(AbstractHttpConfigurer::disable).httpBasic(AbstractHttpConfigurer::disable).sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)).authorizeHttpRequests(authorize -> authorize.requestMatchers(HttpMethod.OPTIONS, "/**").permitAll().requestMatchers("/", "/auth/sign-up", "/auth/sign-in", "/auth/sign-out", "/auth/dev-token", "/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html", "/actuator/**").permitAll().requestMatchers(HttpMethod.GET, "/challenges", "/challenges/visibility-options", "/challenges/policy-options").permitAll().anyRequest().authenticated()).oauth2ResourceServer(oauth2 -> oauth2.bearerTokenResolver(multiBearerTokenResolver).jwt(jwt -> jwt.jwtAuthenticationConverter(customJwtAuthConverter)).authenticationEntryPoint(jwtAuthenticationEntryPoint));
    return http.build();
  }
}
