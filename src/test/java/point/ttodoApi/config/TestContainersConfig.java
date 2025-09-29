package point.ttodoApi.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;


/**
 * TestContainers configuration for integration tests
 * Provides PostgreSQL and Redis containers for testing
 */
@TestConfiguration(proxyBeanMethods = false)
public class TestContainersConfig {

    @SuppressWarnings("resource") // Containers are managed by Spring TestContainer lifecycle
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(DockerImageName.parse("postgres:17-alpine"))
            .withDatabaseName("ttodo_test")
            .withUsername("test_user")
            .withPassword("test_password")
            .withReuse(true);

    @SuppressWarnings("resource") // Containers are managed by Spring TestContainer lifecycle
    static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379)
            .withReuse(true);

    @Bean
    @ServiceConnection
    public PostgreSQLContainer<?> postgresContainer() {
        return POSTGRES;
    }

    @Bean
    @ServiceConnection(name = "redis")
    public GenericContainer<?> redisContainer() {
        return REDIS;
    }
}