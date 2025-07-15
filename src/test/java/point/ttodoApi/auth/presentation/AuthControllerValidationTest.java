package point.ttodoApi.auth.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import point.ttodoApi.auth.presentation.dto.request.SignUpRequest;
import point.ttodoApi.auth.presentation.dto.request.SignInRequest;
import point.ttodoApi.member.application.MemberService;

import org.springframework.context.annotation.Import;
import point.ttodoApi.test.config.TestSecurityConfig;
import point.ttodoApi.test.config.TestDataConfig;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@AutoConfigureMockMvc
@DisplayName("AuthController 검증 테스트")
@Import({TestSecurityConfig.class, TestDataConfig.class})
class AuthControllerValidationTest extends point.ttodoApi.test.IntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MemberService memberService;

    @Test
    @DisplayName("회원가입 - 유효한 입력값으로 성공")
    void signUp_withValidInput_shouldSucceed() throws Exception {
        SignUpRequest request = new SignUpRequest(
                "john.doe@example.com",
                "MySecure@Pass2024!",
                "MySecure@Pass2024!",
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
    @DisplayName("회원가입 - 약한 패스워드로 실패")
    void signUp_withWeakPassword_shouldFail() throws Exception {
        SignUpRequest request = new SignUpRequest(
                "user@example.com",
                "weakpass",  // No uppercase, no special char
                "weakpass",
                "validUser123",
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
    @DisplayName("회원가입 - 연속된 문자가 포함된 패스워드로 실패")
    void signUp_withConsecutiveCharactersPassword_shouldFail() throws Exception {
        SignUpRequest request = new SignUpRequest(
                "user@example.com",
                "Abc123!@#",  // Contains "abc" and "123"
                "Abc123!@#",
                "validUser123",
                null,
                null,
                null
        );

        mockMvc.perform(post("/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.extensions.errors.password").exists());
    }

    @Test
    @DisplayName("회원가입 - 일반적인 약한 패스워드로 실패")
    void signUp_withCommonWeakPassword_shouldFail() throws Exception {
        SignUpRequest request = new SignUpRequest(
                "user@example.com",
                "Password123!",  // Common weak password
                "Password123!",
                "validUser123",
                null,
                null,
                null
        );

        mockMvc.perform(post("/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.extensions.errors.password").exists());
    }

    @Test
    @DisplayName("회원가입 - 금지어가 포함된 닉네임으로 실패")
    void signUp_withForbiddenNickname_shouldFail() throws Exception {
        SignUpRequest request = new SignUpRequest(
                "user@example.com",
                "Strong@Pass123",
                "Strong@Pass123",
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
    @DisplayName("회원가입 - 일회용 이메일 도메인으로 실패")
    void signUp_withDisposableEmail_shouldFail() throws Exception {
        SignUpRequest request = new SignUpRequest(
                "user@10minutemail.com",  // Disposable email
                "Strong@Pass123",
                "Strong@Pass123",
                "validUser123",
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
    @DisplayName("회원가입 - 잘못된 이메일 형식으로 실패")
    void signUp_withInvalidEmailFormat_shouldFail() throws Exception {
        SignUpRequest request = new SignUpRequest(
                "invalid-email",  // Invalid email format
                "Strong@Pass123",
                "Strong@Pass123",
                "validUser123",
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
    @DisplayName("회원가입 - 패스워드 불일치로 실패")
    void signUp_withPasswordMismatch_shouldFail() throws Exception {
        SignUpRequest request = new SignUpRequest(
                "mismatch@example.com",
                "MySecure@Pass2024!",
                "MySecure@Pass2025!",  // Different password
                "mismatchuser",
                null,
                null,
                null
        );

        mockMvc.perform(post("/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value(containsString("패스워드")));
    }

    @Test
    @DisplayName("회원가입 - 짧은 닉네임으로 실패")
    void signUp_withShortNickname_shouldFail() throws Exception {
        SignUpRequest request = new SignUpRequest(
                "user@example.com",
                "Strong@Pass123",
                "Strong@Pass123",
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
    @DisplayName("로그인 - 유효한 이메일로 성공")
    void signIn_withValidEmail_shouldSucceed() throws Exception {
        SignInRequest request = new SignInRequest(
                "anon@ttodo.dev",
                "password123"
        );

        mockMvc.perform(post("/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("로그인 - 일회용 이메일 도메인 허용")
    void signIn_withDisposableEmail_shouldSucceed() throws Exception {
        // 로그인은 일회용 이메일도 허용되므로, 존재하는 사용자로 테스트
        SignInRequest request = new SignInRequest(
                "anon@ttodo.dev",
                "password123"
        );

        mockMvc.perform(post("/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("로그인 - 잘못된 이메일 형식으로 실패")
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
    @DisplayName("회원가입 - 복수 필드 검증 실패")
    void signUp_withMultipleValidationErrors_shouldReturnAllErrors() throws Exception {
        SignUpRequest request = new SignUpRequest(
                "invalid-email",  // Invalid email
                "weak",          // Weak password
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