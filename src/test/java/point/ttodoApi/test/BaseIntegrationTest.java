package point.ttodoApi.test;

import org.junit.jupiter.api.Tag;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;
import point.ttodoApi.config.TestContainersConfig;

/**
 * Base integration test class
 *
 * Provides:
 * - Testcontainers setup for PostgreSQL and Redis
 * - Spring Boot test configuration
 * - Test profile activation
 * - Common test utilities
 *
 * Extend this class for integration tests that need:
 * - Database access
 * - Redis access
 * - Full Spring context
 * - Transactional test methods
 */
@SpringBootTest
@Testcontainers
@Import(TestContainersConfig.class)
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@Transactional
@Tag("integration")
public abstract class BaseIntegrationTest {

    @Container
    @SuppressWarnings("resource") // Container lifecycle managed by Testcontainers
    private static final PostgreSQLContainer<?> POSTGRES_CONTAINER =
        new PostgreSQLContainer<>(DockerImageName.parse("postgres:17-alpine"))
            .withDatabaseName("ttodo_test")
            .withUsername("test_user")
            .withPassword("test_password")
            .withReuse(true);

    @Container
    @SuppressWarnings("resource") // Container lifecycle managed by Testcontainers
    protected static final GenericContainer<?> REDIS_CONTAINER =
        new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379)
            .withReuse(true);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        // PostgreSQL configuration
        registry.add("spring.datasource.url", POSTGRES_CONTAINER::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES_CONTAINER::getUsername);
        registry.add("spring.datasource.password", POSTGRES_CONTAINER::getPassword);

        // Redis configuration
        registry.add("spring.data.redis.host", REDIS_CONTAINER::getHost);
        registry.add("spring.data.redis.port", () -> REDIS_CONTAINER.getMappedPort(6379).toString());

        // Test profile
        registry.add("spring.profiles.active", () -> "test");

        // JWT test keys
        registry.add("jwt.private-key", () -> "classpath:ttodo/jwt/test-private.pem");
        registry.add("jwt.public-key", () -> "classpath:ttodo/jwt/public.pem");
        registry.add("jwt.key-id", () -> "test-rsa-key-id");
    }

    /**
     * Test utility: Get PostgreSQL JDBC URL
     */
    protected static String getPostgresJdbcUrl() {
        return POSTGRES_CONTAINER.getJdbcUrl();
    }

    /**
     * Test utility: Get Redis host
     */
    protected static String getRedisHost() {
        return REDIS_CONTAINER.getHost();
    }

    /**
     * Test utility: Get Redis port
     */
    protected static Integer getRedisPort() {
        return REDIS_CONTAINER.getMappedPort(6379);
    }

    /**
     * Test utility: Check if containers are running
     */
    protected static boolean areContainersRunning() {
        return POSTGRES_CONTAINER.isRunning() && REDIS_CONTAINER.isRunning();
    }
}