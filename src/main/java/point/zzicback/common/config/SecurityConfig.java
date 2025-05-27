package point.zzicback.common.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtValidationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
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
                                "/auth/**",
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

            JwtValidationException validationEx = (JwtValidationException) cause;
            // 1) 만료된 토큰으로부터 subject 추출 (서명 검증 없이 페이로드만 파싱)
            String token = multiBearerTokenResolver.resolve(request);
            String sub = jwtUtil.extractClaim(token, "sub");
            String claimEmail = jwtUtil.extractClaim(token, "email");
            String claimNickname = jwtUtil.extractClaim(token, "nickname");

            // 2) 새 토큰 발급
            String newToken = jwtUtil.generateAccessToken(sub, claimEmail, claimNickname);

            Cookie jwtCookie = cookieUtil.createJwtCookie(newToken);
            response.addCookie(jwtCookie);

             //4) 원래 요청 URI 그대로 307 Temporary Redirect
            String uri = request.getRequestURI()
                    + (request.getQueryString() != null ? "?" + request.getQueryString() : "");
            response.setStatus(HttpServletResponse.SC_TEMPORARY_REDIRECT);
            response.setHeader(HttpHeaders.LOCATION, uri);
        };
    }
}