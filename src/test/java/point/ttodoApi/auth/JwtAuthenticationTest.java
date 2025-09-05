package point.ttodoApi.auth;

import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import point.ttodoApi.auth.config.properties.JwtProperties;
import point.ttodoApi.test.BaseIntegrationTest;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("JWT 인증 테스트")
public class JwtAuthenticationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private JwtProperties jwtProperties;

    @Test
    @DisplayName("헤더: 유효한 Bearer 토큰으로 인증 성공")
    public void testValidBearerTokenInHeader() throws Exception {
        mockMvc.perform(get("/todos")
                .header("Authorization", "Bearer " + validAnonToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("헤더: 토큰 없이 요청 시 401")
    public void testNoTokenInHeader() throws Exception {
        mockMvc.perform(get("/todos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("헤더: 잘못된 토큰으로 요청 시 401")
    public void testInvalidTokenInHeader() throws Exception {
        mockMvc.perform(get("/todos")
                .header("Authorization", "Bearer " + invalidToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("쿠키: 유효한 토큰으로 인증 성공")
    public void testValidTokenInCookie() throws Exception {
        mockMvc.perform(get("/todos")
                .cookie(new Cookie(jwtProperties.accessToken().cookie().name(), validAnonToken)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("쿠키: 토큰 없이 요청 시 401")
    public void testNoTokenInCookie() throws Exception {
        mockMvc.perform(get("/todos"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("쿠키: 잘못된 토큰으로 요청 시 401")
    public void testInvalidTokenInCookie() throws Exception {
        mockMvc.perform(get("/todos")
                .cookie(new Cookie(jwtProperties.accessToken().cookie().name(), invalidToken)))
                .andExpect(status().isUnauthorized());
    }
}