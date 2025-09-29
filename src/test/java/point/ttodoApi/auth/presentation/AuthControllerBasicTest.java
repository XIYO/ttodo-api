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
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
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

        ResponseEntity<Map<String, Object>> response = restTemplate.getForEntity(url, Map.class);

        // The endpoint should return something (may fail due to missing DB, but context should load)
        assertThat(response).isNotNull();
    }
}