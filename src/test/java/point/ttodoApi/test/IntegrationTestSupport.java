package point.ttodoApi.test;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import point.ttodoApi.member.domain.Member;
import point.ttodoApi.member.infrastructure.persistence.MemberRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * 통합 테스트 지원 클래스
 * - Spring Security 포함 전체 컨텍스트 로드
 * - Testcontainers로 PostgreSQL 실행
 * - 실제 사용자 계정(anon@ttodo.dev) 사용
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
public abstract class IntegrationTestSupport {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:17-alpine")
    );

    @LocalServerPort
    protected int port;

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected MemberRepository memberRepository;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    protected Member testMember;

    @BeforeEach
    void setUpTestUser() {
        // anon@ttodo.dev 사용자 확인 또는 생성
        testMember = memberRepository.findByEmail("anon@ttodo.dev")
                .orElseGet(() -> {
                    Member member = Member.builder()
                            .email("anon@ttodo.dev")
                            .password(passwordEncoder.encode("password123"))
                            .nickname("익명사용자")
                            .build();
                    return memberRepository.save(member);
                });
    }

    protected String getBaseUrl() {
        return "http://localhost:" + port;
    }
}