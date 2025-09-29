package point.ttodoApi.test.security;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.User;

import java.util.List;

/**
 * Test configuration for Spring Security
 * Provides UserDetailsService that works with @WithMockUser
 */
@TestConfiguration
public class TestSecurityConfig {

    @Bean
    public UserDetailsService userDetailsService() {
        return username -> {
            // @WithMockUser의 username을 기반으로 User 생성
            // username이 UUID 형식인지 체크
            String userId = username;
            if (!username.contains("-")) {
                // UUID 형식이 아니면 기본값 사용
                userId = "ffffffff-ffff-ffff-ffff-ffffffffffff";
            }
            
            return new User(
                userId, // username as UUID string
                "", // no password for test
                true, true, true, true,
                List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );
        };
    }
}