package point.ttodoApi.test.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import point.ttodoApi.member.domain.Member;
import point.ttodoApi.member.infrastructure.persistence.MemberRepository;


/**
 * 테스트용 데이터 설정
 */
@TestConfiguration
@Profile("test")
public class TestDataConfig {
    
    @Bean
    public CommandLineRunner testDataInitializer(
            MemberRepository memberRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            // anon@ttodo.dev 테스트 사용자 생성
            if (!memberRepository.existsByEmail("anon@ttodo.dev")) {
                Member anonMember = Member.builder()
                        .email("anon@ttodo.dev")
                        .password(passwordEncoder.encode("password123"))
                        .nickname("익명사용자")
                        .build();
                memberRepository.save(anonMember);
            }
        };
    }
}