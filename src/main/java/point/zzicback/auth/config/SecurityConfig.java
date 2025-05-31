package point.zzicback.auth.config;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import point.zzicback.auth.application.AuthTokenService;
import point.zzicback.auth.security.converter.CustomJwtAuthConverter;
import point.zzicback.auth.security.resolver.MultiBearerTokenResolver;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final MultiBearerTokenResolver multiBearerTokenResolver;
    private final CustomJwtAuthConverter customJwtAuthConverter;
    private final AuthTokenService authTokenService;

    @Bean
    public SecurityFilterChain filterChain(
            HttpSecurity http, AuthenticationEntryPoint authenticationEntryPoint) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .sessionManagement(
                        session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(
                        authorize ->
                                authorize
                                        .requestMatchers(
                                                "/",
                                                "/auth/sign-up",
                                                "/auth/sign-in",
                                                "/swagger-ui/**",
                                                "/v3/api-docs/**",
                                                "/swagger-ui.html")
                                        .permitAll()
                                        .anyRequest()
                                        .authenticated())
                .oauth2ResourceServer(
                        oauth2 ->
                                oauth2.bearerTokenResolver(multiBearerTokenResolver)
                                        .jwt(
                                                jwt ->
                                                        jwt.jwtAuthenticationConverter(
                                                                customJwtAuthConverter))
                                        .authenticationEntryPoint(authenticationEntryPoint));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            Throwable cause = authException.getCause();
            if (!(cause instanceof JwtValidationException) && cause != null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            boolean refreshed = authTokenService.refreshTokensIfNeeded(request, response);
            if (!refreshed) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }

            String uri =
                    request.getRequestURI()
                            + (request.getQueryString() != null
                                    ? "?" + request.getQueryString()
                                    : "");
            response.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
            response.setHeader(HttpHeaders.LOCATION, uri);
        };
    }
}
