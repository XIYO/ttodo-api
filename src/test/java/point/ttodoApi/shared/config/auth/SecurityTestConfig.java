package point.ttodoApi.shared.config.auth;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.web.SecurityFilterChain;
import point.ttodoApi.auth.infrastructure.jwt.CustomJwtAuthConverter;

import static org.mockito.Mockito.mock;

/**
 * 테스트용 Security 설정
 * 실제 JWT 검증 없이 @WithMockUser 등의 테스트 어노테이션 사용 가능
 */
@TestConfiguration
@EnableWebSecurity
@Import({CustomJwtAuthConverter.class})
public class SecurityTestConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .anyRequest().authenticated());
        return http.build();
    }
    
    @Bean
    public JwtEncoder jwtEncoder() {
        return mock(JwtEncoder.class);
    }
    
    @Bean
    public JwtDecoder jwtDecoder() {
        return mock(JwtDecoder.class);
    }
}