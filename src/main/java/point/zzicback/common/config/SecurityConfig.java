package point.zzicback.common.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import point.zzicback.common.jwt.TokenService;
import point.zzicback.common.security.etc.CustomJwtAuthConverter;
import point.zzicback.common.security.resolver.MultiBearerTokenResolver;
import point.zzicback.common.utill.CookieUtil;
import point.zzicback.common.utill.JwtUtil;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final MultiBearerTokenResolver multiBearerTokenResolver;
    private final CustomJwtAuthConverter customJwtAuthConverter;
    private final JwtUtil jwtUtil;
    private final CookieUtil cookieUtil;
    private final JwtDecoder jwtDecoder;
    private final ApplicationContext applicationContext;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http, AuthenticationEntryPoint authenticationEntryPoint) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(
                                "/",
                                "/auth/sign-up",
                                "/auth/sign-in",
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-ui.html"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer(oauth2 -> oauth2
                        .bearerTokenResolver(multiBearerTokenResolver)
                        .jwt(jwt -> jwt.jwtAuthenticationConverter(customJwtAuthConverter))
                        .authenticationEntryPoint(authenticationEntryPoint)
                );

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

            String refreshToken = cookieUtil.getRefreshToken(request);
            if (refreshToken == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            try {
                jwtDecoder.decode(refreshToken);
            } catch (JwtException e) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
            String deviceId = jwtUtil.extractClaim(refreshToken, "device");
            // TokenService를 ApplicationContext에서 직접 가져와 사용 (순환참조 방지)
            TokenService tokenService = applicationContext.getBean(TokenService.class);
            TokenService.TokenPair newTokens = tokenService.refreshTokens(deviceId, refreshToken);

            Cookie accessCookie = cookieUtil.createJwtCookie(newTokens.accessToken());
            response.addCookie(accessCookie);
            Cookie refreshCookie = cookieUtil.createRefreshCookie(newTokens.refreshToken());
            response.addCookie(refreshCookie);

            String uri = request.getRequestURI()
                    + (request.getQueryString() != null ? "?" + request.getQueryString() : "");
            response.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
            response.setHeader(HttpHeaders.LOCATION, uri);
        };
    }
}
