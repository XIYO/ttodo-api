package point.ttodoApi.auth.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.*;
import org.testcontainers.utility.DockerImageName;
import point.ttodoApi.auth.presentation.dto.request.*;
import point.ttodoApi.member.domain.Member;
import point.ttodoApi.member.infrastructure.persistence.MemberRepository;
import point.ttodoApi.profile.domain.Profile;
import point.ttodoApi.profile.infrastructure.persistence.ProfileRepository;
import point.ttodoApi.test.config.TestSecurityConfig;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("AuthController Validation Test")
@Import(TestSecurityConfig.class)
@Transactional
@Testcontainers
class AuthControllerValidationTest {
    
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:17-alpine")
    );

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;
    
    @Autowired
    private ProfileRepository profileRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    private void createTestMember(String email, String nickname, String password) {
        Member testMember = Member.builder()
                .email(email)
                .nickname(nickname)
                .password(passwordEncoder.encode(password))
                .build();
        memberRepository.save(testMember);
        
        Profile testProfile = new Profile(testMember.getId());
        profileRepository.save(testProfile);
    }

    @Test
    @DisplayName("Sign up with valid input should succeed")
    void signUp_withValidInput_shouldSucceed() throws Exception {
        SignUpRequest request = new SignUpRequest(
                "john.doe.test1@example.com",
                "pass1234",
                "pass1234",
                "johndoe",
                "안녕하세요 새로운 사용자입니다",
                "Asia/Seoul",
                "ko_KR"
        );

        mockMvc.perform(post("/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Sign up with too short password should fail")
    void signUp_withTooShortPassword_shouldFail() throws Exception {
        SignUpRequest request = new SignUpRequest(
                "test.person@example.com",
                "abc",  // Too short (less than 4 chars)
                "abc",
                "validPerson",
                null,
                null,
                null
        );

        mockMvc.perform(post("/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.extensions.errors.password").exists());
    }

    @Test
    @DisplayName("Sign up with password containing consecutive characters should succeed")
    void signUp_withConsecutiveCharactersPassword_shouldSucceed() throws Exception {
        SignUpRequest request = new SignUpRequest(
                "test.person@example.com",
                "Abc123!@#",  // Now allowed
                "Abc123!@#",
                "validPerson",
                null,
                null,
                null
        );

        mockMvc.perform(post("/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Sign up with common weak password should succeed")
    void signUp_withCommonWeakPassword_shouldSucceed() throws Exception {
        SignUpRequest request = new SignUpRequest(
                "test.person@example.com",
                "Password123!",  // Now allowed
                "Password123!",
                "validPerson",
                null,
                null,
                null
        );

        mockMvc.perform(post("/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Sign up with nickname containing forbidden words should fail")
    void signUp_withForbiddenNickname_shouldFail() throws Exception {
        SignUpRequest request = new SignUpRequest(
                "test.person@example.com",
                "pass1234",
                "pass1234",
                "admin",  // Forbidden word
                null,
                null,
                null
        );

        mockMvc.perform(post("/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.extensions.errors.nickname").exists())
                .andExpect(jsonPath("$.extensions.errors.nickname").value(containsString("forbidden")));
    }

    @Test
    @DisplayName("Sign up with disposable email domain should fail")
    void signUp_withDisposableEmail_shouldFail() throws Exception {
        SignUpRequest request = new SignUpRequest(
                "person@10minutemail.com",  // Disposable email
                "pass1234",
                "pass1234",
                "validPerson",
                null,
                null,
                null
        );

        mockMvc.perform(post("/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.extensions.errors.email").exists())
                .andExpect(jsonPath("$.extensions.errors.email").value(containsString("Disposable")));
    }

    @Test
    @DisplayName("Sign up with invalid email format should fail")
    void signUp_withInvalidEmailFormat_shouldFail() throws Exception {
        SignUpRequest request = new SignUpRequest(
                "invalid-email",  // Invalid email format
                "pass1234",
                "pass1234",
                "validPerson",
                null,
                null,
                null
        );

        mockMvc.perform(post("/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.extensions.errors.email").exists());
    }

    @Test
    @DisplayName("Sign up with password mismatch should fail")
    void signUp_withPasswordMismatch_shouldFail() throws Exception {
        SignUpRequest request = new SignUpRequest(
                "mismatch.test@example.com",
                "pass1234",
                "pass1235",  // Different password
                "mismatchuser",
                null,
                null,
                null
        );

        mockMvc.perform(post("/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.extensions.errors.confirmPassword").exists());
    }

    @Test
    @DisplayName("Sign up with short nickname should fail")
    void signUp_withShortNickname_shouldFail() throws Exception {
        SignUpRequest request = new SignUpRequest(
                "test.person@example.com",
                "pass1234",
                "pass1234",
                "u",  // Too short
                null,
                null,
                null
        );

        mockMvc.perform(post("/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.extensions.errors.nickname").exists());
    }

    @Test
    @DisplayName("Sign in with valid email should succeed")
    void signIn_withValidEmail_shouldSucceed() throws Exception {
        // 테스트 사용자 생성
        createTestMember("test.signin@example.com", "testuser", "password123");
        
        SignInRequest request = new SignInRequest(
                "test.signin@example.com",
                "password123"
        );

        mockMvc.perform(post("/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())  // 응답 내용 출력
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Sign in with disposable email domain allowed")
    void signIn_withDisposableEmail_shouldSucceed() throws Exception {
        // 테스트 사용자 생성 (일회용 이메일)
        createTestMember("testuser@10minutemail.com", "testuser", "password123");
        
        SignInRequest request = new SignInRequest(
                "testuser@10minutemail.com",
                "password123"
        );

        mockMvc.perform(post("/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Sign in with invalid email format should fail")
    void signIn_withInvalidEmailFormat_shouldFail() throws Exception {
        SignInRequest request = new SignInRequest(
                "invalid-email",
                "password"
        );

        mockMvc.perform(post("/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.extensions.errors.email").exists());
    }

    @Test
    @DisplayName("Sign up with multiple field validation failures")
    void signUp_withMultipleValidationErrors_shouldReturnAllErrors() throws Exception {
        SignUpRequest request = new SignUpRequest(
                "invalid-email",  // Invalid email
                "abc",           // Too short password (less than 4)
                "different",     // Password mismatch
                "a",            // Too short nickname
                null,
                null,
                null
        );

        mockMvc.perform(post("/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.extensions.errors.email").exists())
                .andExpect(jsonPath("$.extensions.errors.password").exists())
                .andExpect(jsonPath("$.extensions.errors.nickname").exists());
    }
}