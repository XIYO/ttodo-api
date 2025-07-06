package point.zzicback.test.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import point.zzicback.auth.domain.MemberPrincipal;
import point.zzicback.member.domain.Member;
import point.zzicback.member.infrastructure.persistence.MemberRepository;

import java.util.List;

/**
 * 테스트용 Security 설정
 */
@TestConfiguration
public class TestSecurityConfig {
    
    @Bean
    public UserDetailsService testUserDetailsService(MemberRepository memberRepository) {
        return username -> {
            Member member = memberRepository.findByEmail(username)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
            
            return MemberPrincipal.from(
                    member.getId(),
                    member.getEmail(),
                    member.getNickname(),
                    "Asia/Seoul", // 기본 타임존
                    "ko-KR", // 기본 로케일
                    List.of(new SimpleGrantedAuthority("ROLE_USER"))
            );
        };
    }
}