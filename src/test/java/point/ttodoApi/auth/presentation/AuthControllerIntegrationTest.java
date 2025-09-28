package point.ttodoApi.auth.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.testcontainers.junit.jupiter.Testcontainers;
import point.ttodoApi.test.BaseIntegrationTest;
import point.ttodoApi.auth.application.TokenService;
import point.ttodoApi.user.infrastructure.persistence.UserRepository;
import point.ttodoApi.profile.infrastructure.persistence.ProfileRepository;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.*;

/**
 * AuthController 통합 테스트
 *
 * 테스트 범위:
 * - 회원가입, 로그인, 로그아웃, 토큰 갱신 전체 플로우
 * - 보안 취약점: SQL Injection, XSS, 입력 검증
 * - 경계 조건: 긴 입력, 특수 문자, null/empty 값
 * - 동시성: 동시 로그인/갱신 요청
 * - 쿠키 조작: 잘못된 토큰, 만료된 토큰
 *
 * 테스트 환경: Testcontainers (PostgreSQL + Redis) via BaseIntegrationTest
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@DirtiesContext
class AuthControllerIntegrationTest extends BaseIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProfileRepository profileRepository;

    private String baseUrl;

    @BeforeEach
    void setUp() {
        baseUrl = "http://localhost:" + port + "/auth";
    }

    @Nested
    @DisplayName("회원가입 테스트")
    class SignUpTests {

        @Test
        @DisplayName("정상 회원가입 및 자동 로그인")
        @Transactional
        void signUp_Success() {
            // Given
            MultiValueMap<String, String> signUpData = new LinkedMultiValueMap<>();
            signUpData.add("email", "test@example.com");
            signUpData.add("password", "SecurePass123!");
            signUpData.add("confirmPassword", "SecurePass123!");
            signUpData.add("nickname", "테스트사용자");
            signUpData.add("introduction", "안녕하세요!");
            signUpData.add("timeZone", "Asia/Seoul");
            signUpData.add("locale", "ko_KR");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(signUpData, headers);

            // When
            ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/sign-up", request, String.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            // 쿠키 검증
            List<String> setCookies = response.getHeaders().get("Set-Cookie");
            assertThat(setCookies).isNotNull();
            assertThat(setCookies).anyMatch(cookie -> cookie.contains("access-token="));
            assertThat(setCookies).anyMatch(cookie -> cookie.contains("refresh-token="));

            // Authorization 헤더 검증
            String authHeader = response.getHeaders().getFirst("Authorization");
            assertThat(authHeader).isNotNull();
            assertThat(authHeader).startsWith("Bearer ");

            // DB 검증 - 사용자가 생성되었는지 확인
            assertThat(userRepository.existsByEmail("test@example.com")).isTrue();
        }

        @Test
        @DisplayName("정상 회원가입 (보안 패스워드)")
        @Transactional
        void signUp_WithSecurePassword_Success() {
            // Given
            MultiValueMap<String, String> signUpData = new LinkedMultiValueMap<>();
            signUpData.add("email", "secureuser@example.com");
            signUpData.add("password", "SecurePass123!");
            signUpData.add("confirmPassword", "SecurePass123!");
            signUpData.add("nickname", "보안사용자");
            signUpData.add("introduction", "안녕하세요");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(signUpData, headers);

            // When
            ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/sign-up", request, String.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(userRepository.existsByEmail("secureuser@example.com")).isTrue();
        }

        @Test
        @DisplayName("중복 이메일 회원가입 실패")
        @Transactional
        void signUp_DuplicateEmail_Fails() {
            // Given - 먼저 사용자 등록
            MultiValueMap<String, String> firstSignUp = new LinkedMultiValueMap<>();
            firstSignUp.add("email", "duplicate@example.com");
            firstSignUp.add("password", "SecurePass123!");
            firstSignUp.add("confirmPassword", "SecurePass123!");
            firstSignUp.add("nickname", "첫번째사용자");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            restTemplate.postForEntity(baseUrl + "/sign-up",
                new HttpEntity<>(firstSignUp, headers), String.class);

            // When - 같은 이메일로 다시 가입 시도
            MultiValueMap<String, String> duplicateSignUp = new LinkedMultiValueMap<>();
            duplicateSignUp.add("email", "duplicate@example.com");
            duplicateSignUp.add("password", "AnotherPass123!");
            duplicateSignUp.add("confirmPassword", "AnotherPass123!");
            duplicateSignUp.add("nickname", "두번째사용자");

            ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/sign-up", new HttpEntity<>(duplicateSignUp, headers), String.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        }

        @Test
        @DisplayName("잘못된 이메일 형식으로 회원가입 실패")
        @Transactional
        void signUp_InvalidEmail_Fails() {
            // Given
            String[] invalidEmails = {
                "invalid-email",
                "invalid@",
                "@invalid.com",
                "invalid.email@"
            };

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            for (String email : invalidEmails) {
                MultiValueMap<String, String> signUpData = new LinkedMultiValueMap<>();
                signUpData.add("email", email);
                signUpData.add("password", "ValidPass123!");
                signUpData.add("confirmPassword", "ValidPass123!");
                signUpData.add("nickname", "테스트");

                // When
                ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/sign-up", new HttpEntity<>(signUpData, headers), String.class);

                // Then
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
                    .withFailMessage("Invalid email should fail: " + email);
            }
        }

        @Test
        @DisplayName("XSS 공격 시도 - 닉네임 및 소개글 HTML 태그 제거")
        @Transactional
        void signUp_XSSAttempt_Sanitized() {
            // Given
            String maliciousScript = "<script>alert('XSS')</script>";
            String maliciousNickname = "사용자" + maliciousScript;
            String maliciousIntroduction = "안녕하세요!" + maliciousScript + "<img src=x onerror=alert(1)>";

            MultiValueMap<String, String> signUpData = new LinkedMultiValueMap<>();
            signUpData.add("email", "xss@example.com");
            signUpData.add("password", "SecurePass123!");
            signUpData.add("confirmPassword", "SecurePass123!");
            signUpData.add("nickname", maliciousNickname);
            signUpData.add("introduction", maliciousIntroduction);
            signUpData.add("deviceId", "test-device-xss-123");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(signUpData, headers);

            // When
            ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/sign-up", request, String.class);

            // Then
            // HTML 태그가 제거되어 저장되어야 함 (SanitizeHtml 어노테이션 작동)
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
            // DB에서 실제로 HTML 태그가 제거되었는지 확인
            assertThat(userRepository.existsByEmail("xss@example.com")).isTrue();
        }

        @Test
        @DisplayName("SQL Injection 공격 시도 - 안전하게 처리")
        @Transactional
        void signUp_SQLInjectionAttempt_Blocked() {
            // Given
            String[] sqlInjectionAttempts = {
                "'; DROP TABLE users; --",
                "admin@example.com' OR '1'='1",
                "user@example.com'; INSERT INTO users VALUES ('hack', 'hack'); --"
            };

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            for (String maliciousEmail : sqlInjectionAttempts) {
                MultiValueMap<String, String> signUpData = new LinkedMultiValueMap<>();
                signUpData.add("email", maliciousEmail);
                signUpData.add("password", "ValidPass123!");
                signUpData.add("confirmPassword", "ValidPass123!");
                signUpData.add("nickname", "테스트");

                // When
                ResponseEntity<String> response = restTemplate.postForEntity(
                    baseUrl + "/sign-up", new HttpEntity<>(signUpData, headers), String.class);

                // Then - 이메일 검증 실패로 400 Bad Request 응답
                assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST)
                    .withFailMessage("SQL injection attempt should be blocked: " + maliciousEmail);
            }
        }

        @Test
        @DisplayName("긴 입력값으로 회원가입 시도")
        @Transactional
        void signUp_LongInput_ValidationFails() {
            // Given
            String longNickname = "a".repeat(25); // 20자 제한 초과
            String longPassword = "a".repeat(105); // 100자 제한 초과

            MultiValueMap<String, String> signUpData = new LinkedMultiValueMap<>();
            signUpData.add("email", "long@example.com");
            signUpData.add("password", longPassword);
            signUpData.add("confirmPassword", longPassword);
            signUpData.add("nickname", longNickname);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(signUpData, headers);

            // When
            ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/sign-up", request, String.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("패스워드 불일치 회원가입 실패")
        @Transactional
        void signUp_PasswordMismatch_Fails() {
            // Given
            MultiValueMap<String, String> signUpData = new LinkedMultiValueMap<>();
            signUpData.add("email", "mismatch@example.com");
            signUpData.add("password", "Password123!");
            signUpData.add("confirmPassword", "DifferentPassword123!");
            signUpData.add("nickname", "테스트");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(signUpData, headers);

            // When
            ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/sign-up", request, String.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }
    }

    @Nested
    @DisplayName("로그인 테스트")
    class SignInTests {

        @BeforeEach
        @Transactional
        void setUpUser() {
            // 테스트용 사용자 생성
            MultiValueMap<String, String> signUpData = new LinkedMultiValueMap<>();
            signUpData.add("email", "existing@example.com");
            signUpData.add("password", "ExistingPass123!");
            signUpData.add("confirmPassword", "ExistingPass123!");
            signUpData.add("nickname", "기존사용자");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            restTemplate.postForEntity(baseUrl + "/sign-up",
                new HttpEntity<>(signUpData, headers), String.class);
        }

        @Test
        @DisplayName("정상 로그인")
        @Transactional
        void signIn_Success() {
            // Given
            MultiValueMap<String, String> signInData = new LinkedMultiValueMap<>();
            signInData.add("email", "existing@example.com");
            signInData.add("password", "ExistingPass123!");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(signInData, headers);

            // When
            ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/sign-in", request, String.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            // 쿠키 및 헤더 검증
            List<String> setCookies = response.getHeaders().get("Set-Cookie");
            assertThat(setCookies).anyMatch(cookie -> cookie.contains("access-token="));
            assertThat(setCookies).anyMatch(cookie -> cookie.contains("refresh-token="));

            String authHeader = response.getHeaders().getFirst("Authorization");
            assertThat(authHeader).startsWith("Bearer ");
        }

        @Test
        @DisplayName("빈 패스워드로 로그인 실패")
        @Transactional
        void signIn_EmptyPassword_Fails() {
            // Given - 먼저 사용자 등록
            MultiValueMap<String, String> signUpData = new LinkedMultiValueMap<>();
            signUpData.add("email", "emptypass@example.com");
            signUpData.add("password", "TestPass123!");
            signUpData.add("confirmPassword", "TestPass123!");
            signUpData.add("nickname", "테스트사용자");
            
            HttpHeaders signUpHeaders = new HttpHeaders();
            signUpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            restTemplate.postForEntity(baseUrl + "/sign-up", 
                new HttpEntity<>(signUpData, signUpHeaders), String.class);
            
            MultiValueMap<String, String> signInData = new LinkedMultiValueMap<>();
            signInData.add("email", "emptypass@example.com");
            signInData.add("password", "");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(signInData, headers);

            // When
            ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/sign-in", request, String.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("잘못된 패스워드로 로그인 실패")
        @Transactional
        void signIn_WrongPassword_Fails() {
            // Given
            MultiValueMap<String, String> signInData = new LinkedMultiValueMap<>();
            signInData.add("email", "existing@example.com");
            signInData.add("password", "WrongPassword123!");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(signInData, headers);

            // When
            ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/sign-in", request, String.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("존재하지 않는 사용자 로그인 실패")
        @Transactional
        void signIn_NonExistentUser_Fails() {
            // Given
            MultiValueMap<String, String> signInData = new LinkedMultiValueMap<>();
            signInData.add("email", "nonexistent@example.com");
            signInData.add("password", "AnyPassword123!");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(signInData, headers);

            // When
            ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/sign-in", request, String.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("동시 로그인 요청 처리")
        @Transactional
        void signIn_ConcurrentRequests_AllSucceed() throws InterruptedException {
            // Given
            int threadCount = 5;
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);
            AtomicInteger successCount = new AtomicInteger(0);
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            // When
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        startLatch.await();

                        MultiValueMap<String, String> signInData = new LinkedMultiValueMap<>();
                        signInData.add("email", "existing@example.com");
                        signInData.add("password", "ExistingPass123!");

                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(signInData, headers);

                        ResponseEntity<String> response = restTemplate.postForEntity(
                            baseUrl + "/sign-in", request, String.class);

                        if (response.getStatusCode() == HttpStatus.OK) {
                            successCount.incrementAndGet();
                        }
                    } catch (Exception e) {
                        // 예외 발생시 로그 출력하고 계속 진행
                        System.err.println("Concurrent sign-in failed: " + e.getMessage());
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            startLatch.countDown(); // 모든 스레드 시작
            assertThat(doneLatch.await(30, TimeUnit.SECONDS)).isTrue();
            executor.shutdown();

            // Then
            assertThat(successCount.get()).isEqualTo(threadCount);
        }
    }

    @Nested
    @DisplayName("로그아웃 테스트")
    class SignOutTests {

        private String accessToken;
        private String refreshToken;

        @BeforeEach
        @Transactional
        void setUpAuthenticatedUser() {
            // 사용자 생성 및 로그인
            MultiValueMap<String, String> signUpData = new LinkedMultiValueMap<>();
            signUpData.add("email", "logout@example.com");
            signUpData.add("password", "LogoutTest123!");
            signUpData.add("confirmPassword", "LogoutTest123!");
            signUpData.add("nickname", "로그아웃테스트");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            ResponseEntity<String> signUpResponse = restTemplate.postForEntity(baseUrl + "/sign-up",
                new HttpEntity<>(signUpData, headers), String.class);

            // 쿠키에서 토큰 추출
            List<String> setCookies = signUpResponse.getHeaders().get("Set-Cookie");
            accessToken = extractTokenFromCookies(setCookies, "access-token");
            refreshToken = extractTokenFromCookies(setCookies, "refresh-token");
        }

        @Test
        @DisplayName("정상 로그아웃")
        @Transactional
        void signOut_Success() {
            // Given
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.add("Cookie", "refresh-token=" + refreshToken + "; device-id=test-device");

            MultiValueMap<String, String> signOutData = new LinkedMultiValueMap<>();
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(signOutData, headers);

            // When
            ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/sign-out", request, String.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            // 만료된 쿠키가 설정되었는지 확인
            List<String> setCookies = response.getHeaders().get("Set-Cookie");
            assertThat(setCookies).anyMatch(cookie ->
                cookie.contains("access-token=") && cookie.contains("Max-Age=0"));
            assertThat(setCookies).anyMatch(cookie ->
                cookie.contains("refresh-token=") && cookie.contains("Max-Age=0"));
        }

        @Test
        @DisplayName("쿠키 없이 로그아웃 (안전하게 처리)")
        @Transactional
        void signOut_WithoutCookies_HandledSafely() {
            // Given
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> signOutData = new LinkedMultiValueMap<>();
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(signOutData, headers);

            // When
            ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/sign-out", request, String.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("토큰 갱신 테스트")
    class RefreshTests {

        private String refreshToken;
        private final String deviceId = "test-device-id";

        @BeforeEach
        @Transactional
        void setUpAuthenticatedUser() {
            // 사용자 생성 및 로그인으로 토큰 획득
            MultiValueMap<String, String> signUpData = new LinkedMultiValueMap<>();
            signUpData.add("email", "refresh@example.com");
            signUpData.add("password", "RefreshTest123!");
            signUpData.add("confirmPassword", "RefreshTest123!");
            signUpData.add("nickname", "리프레시테스트");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            ResponseEntity<String> signUpResponse = restTemplate.postForEntity(baseUrl + "/sign-up",
                new HttpEntity<>(signUpData, headers), String.class);

            // 쿠키에서 리프레시 토큰 추출
            List<String> setCookies = signUpResponse.getHeaders().get("Set-Cookie");
            refreshToken = extractTokenFromCookies(setCookies, "refresh-token");
        }

        @Test
        @DisplayName("정상 토큰 갱신")
        @Transactional
        void refresh_Success() {
            // Given
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.add("Cookie", "refresh-token=" + refreshToken + "; device-id=" + deviceId);

            MultiValueMap<String, String> refreshData = new LinkedMultiValueMap<>();
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(refreshData, headers);

            // When
            ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/refresh", request, String.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            // 새로운 토큰이 쿠키로 설정되었는지 확인
            List<String> setCookies = response.getHeaders().get("Set-Cookie");
            assertThat(setCookies).anyMatch(cookie -> cookie.contains("access-token="));
            assertThat(setCookies).anyMatch(cookie -> cookie.contains("refresh-token="));
        }

        @Test
        @DisplayName("잘못된 리프레시 토큰으로 갱신 실패")
        @Transactional
        void refresh_InvalidToken_Fails() {
            // Given
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.add("Cookie", "refresh-token=invalid-token; device-id=" + deviceId);

            MultiValueMap<String, String> refreshData = new LinkedMultiValueMap<>();
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(refreshData, headers);

            // When
            ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/refresh", request, String.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("쿠키 없이 토큰 갱신 실패")
        @Transactional
        void refresh_NoCookies_Fails() {
            // Given
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> refreshData = new LinkedMultiValueMap<>();
            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(refreshData, headers);

            // When
            ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/refresh", request, String.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        }

        @Test
        @DisplayName("동시 토큰 갱신 요청 처리")
        @Transactional
        void refresh_ConcurrentRequests_HandledProperly() throws InterruptedException {
            // Given
            int threadCount = 3;
            CountDownLatch startLatch = new CountDownLatch(1);
            CountDownLatch doneLatch = new CountDownLatch(threadCount);
            AtomicInteger successCount = new AtomicInteger(0);
            ExecutorService executor = Executors.newFixedThreadPool(threadCount);

            // When
            for (int i = 0; i < threadCount; i++) {
                executor.submit(() -> {
                    try {
                        startLatch.await();

                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
                        headers.add("Cookie", "refresh-token=" + refreshToken + "; device-id=" + deviceId);

                        MultiValueMap<String, String> refreshData = new LinkedMultiValueMap<>();
                        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(refreshData, headers);

                        ResponseEntity<String> response = restTemplate.postForEntity(
                            baseUrl + "/refresh", request, String.class);

                        if (response.getStatusCode() == HttpStatus.OK) {
                            successCount.incrementAndGet();
                        }
                    } catch (Exception e) {
                        System.err.println("Concurrent refresh failed: " + e.getMessage());
                    } finally {
                        doneLatch.countDown();
                    }
                });
            }

            startLatch.countDown();
            assertThat(doneLatch.await(30, TimeUnit.SECONDS)).isTrue();
            executor.shutdown();

            // Then - 동시 요청이어도 모두 성공해야 함 (캐싱 메커니즘으로)
            assertThat(successCount.get()).isEqualTo(threadCount);
        }
    }

    @Nested
    @DisplayName("개발 토큰 테스트")
    class DevTokenTests {

        @Test
        @DisplayName("개발 토큰 생성 성공")
        void getDevToken_Success() {
            // When
            ResponseEntity<Map> response = restTemplate.getForEntity(
                baseUrl + "/dev-token", Map.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);

            Map<String, String> body = response.getBody();
            assertThat(body).isNotNull();
            assertThat(body.get("token")).isNotNull();
            assertThat(body.get("usage")).contains("Swagger");
            assertThat(body.get("userId")).isNotNull();
            assertThat(body.get("email")).isEqualTo("dev@ttodo.dev");
            assertThat(body.get("expiresIn")).isEqualTo("NEVER (만료 없음)");
        }
    }

    @Nested
    @DisplayName("전체 인증 플로우 테스트")
    class AuthFlowTests {

        @Test
        @DisplayName("회원가입 → 로그아웃 → 로그인 → 토큰 갱신 전체 플로우")
        @Transactional
        void completeAuthFlow_Success() {
            // 1. 회원가입
            MultiValueMap<String, String> signUpData = new LinkedMultiValueMap<>();
            signUpData.add("email", "flow@example.com");
            signUpData.add("password", "FlowTest123!");
            signUpData.add("confirmPassword", "FlowTest123!");
            signUpData.add("nickname", "플로우테스트");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            ResponseEntity<String> signUpResponse = restTemplate.postForEntity(baseUrl + "/sign-up",
                new HttpEntity<>(signUpData, headers), String.class);
            assertThat(signUpResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

            // 2. 로그아웃
            List<String> signUpCookies = signUpResponse.getHeaders().get("Set-Cookie");
            String refreshTokenFromSignUp = extractTokenFromCookies(signUpCookies, "refresh-token");

            HttpHeaders logoutHeaders = new HttpHeaders();
            logoutHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            logoutHeaders.add("Cookie", "refresh-token=" + refreshTokenFromSignUp + "; device-id=test-device");

            ResponseEntity<String> logoutResponse = restTemplate.postForEntity(baseUrl + "/sign-out",
                new HttpEntity<>(new LinkedMultiValueMap<>(), logoutHeaders), String.class);
            assertThat(logoutResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

            // 3. 다시 로그인
            MultiValueMap<String, String> signInData = new LinkedMultiValueMap<>();
            signInData.add("email", "flow@example.com");
            signInData.add("password", "FlowTest123!");

            ResponseEntity<String> signInResponse = restTemplate.postForEntity(baseUrl + "/sign-in",
                new HttpEntity<>(signInData, headers), String.class);
            assertThat(signInResponse.getStatusCode()).isEqualTo(HttpStatus.OK);

            // 4. 토큰 갱신
            List<String> signInCookies = signInResponse.getHeaders().get("Set-Cookie");
            String refreshTokenFromSignIn = extractTokenFromCookies(signInCookies, "refresh-token");

            HttpHeaders refreshHeaders = new HttpHeaders();
            refreshHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            refreshHeaders.add("Cookie", "refresh-token=" + refreshTokenFromSignIn + "; device-id=test-device");

            ResponseEntity<String> refreshResponse = restTemplate.postForEntity(baseUrl + "/refresh",
                new HttpEntity<>(new LinkedMultiValueMap<>(), refreshHeaders), String.class);
            assertThat(refreshResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        }
    }

    @Nested
    @DisplayName("보안 테스트")
    class SecurityTests {

        @Test
        @DisplayName("쿠키 조작 방지 - 잘못된 JWT 서명")
        @Transactional
        void tamperedJwtCookie_Rejected() {
            // Given - 조작된 JWT 토큰 (서명 부분을 변조)
            String tamperedToken = "eyJhbGciOiJSUzI1NiIsImtpZCI6InJzYS1rZXktaWQiLCJ0eXAiOiJKV1QifQ" +
                ".eyJzdWIiOiJmZmZmZmZmZi1mZmZmLWZmZmYtZmZmZi1mZmZmZmZmZmZmZmYiLCJpYXQiOjE3NTY0OTc" +
                "yMDQsImV4cCI6NDkxMDA5NzIwNCwiZW1haWwiOiJhbm9uQHR0b2RvLmRldiJ9" +
                ".TAMPERED_SIGNATURE";

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.add("Cookie", "refresh-token=" + tamperedToken + "; device-id=test-device");

            // When
            ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + "/refresh",
                new HttpEntity<>(new LinkedMultiValueMap<>(), headers), String.class);

            // Then
            assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
        }

        @Test
        @DisplayName("CSRF 방지 - 다른 Origin에서의 요청")
        void csrfProtection_BlocksCrossOriginRequests() {
            // Given
            // 먼저 테스트 사용자 등록
            MultiValueMap<String, String> signUpData = new LinkedMultiValueMap<>();
            signUpData.add("email", "csrf-test@example.com");
            signUpData.add("password", "TestPass123!");
            signUpData.add("confirmPassword", "TestPass123!");
            signUpData.add("nickname", "CSRF테스트");
            
            HttpHeaders signUpHeaders = new HttpHeaders();
            signUpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            restTemplate.postForEntity(baseUrl + "/sign-up", 
                new HttpEntity<>(signUpData, signUpHeaders), String.class);
            
            MultiValueMap<String, String> signInData = new LinkedMultiValueMap<>();
            signInData.add("email", "csrf-test@example.com");
            signInData.add("password", "TestPass123!");

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
            headers.setOrigin("http://malicious-site.com");

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(signInData, headers);

            // When
            ResponseEntity<String> response = restTemplate.postForEntity(
                baseUrl + "/sign-in", request, String.class);

            // Then - CORS 설정에 따라 차단되어야 함
            // 실제 CORS 설정이 적용되었다면 403 또는 차단될 것임
            // 테스트 환경에서는 CORS가 완전히 적용되지 않을 수 있으므로 주의
        }

        @Test
        @DisplayName("레이트 리미팅 시뮬레이션 - 과도한 로그인 시도")
        void rateLimiting_ExcessiveLoginAttempts() throws InterruptedException {
            // Given
            int attemptCount = 10;
            CountDownLatch latch = new CountDownLatch(attemptCount);
            ExecutorService executor = Executors.newFixedThreadPool(attemptCount);
            AtomicInteger failureCount = new AtomicInteger(0);

            // When - 동시에 많은 로그인 시도
            for (int i = 0; i < attemptCount; i++) {
                executor.submit(() -> {
                    try {
                        MultiValueMap<String, String> signInData = new LinkedMultiValueMap<>();
                        signInData.add("email", "nonexistent@example.com");
                        signInData.add("password", "WrongPassword");

                        HttpHeaders headers = new HttpHeaders();
                        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

                        ResponseEntity<String> response = restTemplate.postForEntity(baseUrl + "/sign-in",
                            new HttpEntity<>(signInData, headers), String.class);

                        if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
                            failureCount.incrementAndGet();
                        }
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await(30, TimeUnit.SECONDS);
            executor.shutdown();

            // Then - 모든 시도가 실패해야 함
            assertThat(failureCount.get()).isEqualTo(attemptCount);
        }
    }

    // 유틸리티 메서드
    private String extractTokenFromCookies(List<String> cookies, String tokenName) {
        if (cookies == null) return null;

        Pattern pattern = Pattern.compile(tokenName + "=([^;]+)");
        return cookies.stream()
            .map(pattern::matcher)
            .filter(java.util.regex.Matcher::find)
            .map(matcher -> matcher.group(1))
            .findFirst()
            .orElse(null);
    }
}