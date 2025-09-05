package point.ttodoApi.test;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

/**
 * 모든 통합 테스트의 기본 클래스
 * Testcontainers를 사용하여 PostgreSQL과 Redis를 자동으로 구성
 * 테스트용 JWT 토큰을 properties에서 주입받아 사용
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@AutoConfigureMockMvc
public abstract class BaseIntegrationTest {
    
    /**
     * anon@ttodo.dev 계정의 유효한 JWT 토큰
     * application.yml의 test.auth.anon-token에서 주입
     */
    @Value("${test.auth.anon-token}")
    protected String validAnonToken;
    
    /**
     * 잘못된 서명의 JWT 토큰 (테스트용)
     * application.yml의 test.auth.invalid-token에서 주입
     */
    @Value("${test.auth.invalid-token}")
    protected String invalidToken;
    
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
        DockerImageName.parse("postgres:17-alpine")
    );
    
    @Container
    static GenericContainer<?> redis = new GenericContainer<>(
        DockerImageName.parse("redis:7-alpine")
    ).withExposedPorts(6379);
    
    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        // Redis 연결 정보 동적 설정
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
        
        // JWT 토큰 설정 (테스트용)
        registry.add("jwt.access-token.expiration", () -> "3600");
        registry.add("jwt.refresh-token.expiration", () -> "86400");
    }
}