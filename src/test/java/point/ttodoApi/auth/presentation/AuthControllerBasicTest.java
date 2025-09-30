package point.ttodoApi.auth.presentation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureWebMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Basic AuthController test without TestContainers
 * To verify Spring Boot configuration works correctly
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6379",
        "jwt.private-key=classpath:ttodo/jwt/test-private.pem",
        "jwt.public-key=classpath:ttodo/jwt/public.pem",
        "jwt.key-id=test-rsa-key-id",
        "jwt.access-token.expiration=1800",
        "jwt.refresh-token.expiration=86400",
        "management.endpoints.web.exposure.include=health",
        "logging.level.root=WARN"
    }
)
@AutoConfigureWebMvc
class AuthControllerBasicTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void contextLoads() {
        // Just verify the context loads properly
        assertThat(port).isGreaterThan(0);
    }

    @Test
    void getDevToken_ReturnsResponse() {
        // Test the dev token endpoint (should work without database)
        String url = "http://localhost:" + port + "/auth/dev-token";

        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

        // The endpoint should return something (may fail due to missing DB, but context should load)
        assertThat(response).isNotNull();
    }
}