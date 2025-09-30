package point.ttodoApi.shared.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * 테스트(WebMvcTest) 슬라이스에서 모든 요청을 허용하기 위한 Security 설정.
 * 실제 SecurityConfig의 복잡한 JWT/커스텀 필터 체인을 우회하여
 * Controller 단위 테스트 집중도를 높인다.
 * 통합 테스트(BaseIntegrationTest)는 실제 SecurityConfig를 사용해야 한다.
 */
@TestConfiguration
@EnableWebSecurity
@EnableMethodSecurity
public class ApiSecurityTestConfig {

  /**
   * 모든 요청 permitAll.
   * 필요 시 특정 엔드포인트만 제한하도록 조정 가능.
   */
  @Bean
  @Primary
  public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
    return http
        .csrf(AbstractHttpConfigurer::disable)
        .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
        .build();
  }
}
