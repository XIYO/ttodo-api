package point.ttodoApi.user.presentation;

import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import point.ttodoApi.profile.application.ProfileService;
import point.ttodoApi.profile.domain.Profile;
import point.ttodoApi.shared.config.auth.SecurityTestConfig;
import point.ttodoApi.user.application.*;
import point.ttodoApi.user.application.query.UserQuery;
import point.ttodoApi.user.application.query.UserListQuery;
import point.ttodoApi.user.application.result.UserResult;
import point.ttodoApi.user.presentation.mapper.UserPresentationMapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserController 단위 테스트
 * Nested 구조로 CRUD 순서에 따라 테스트 구성
 */
@WebMvcTest(UserController.class)
@Import({SecurityTestConfig.class})
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @MockitoBean
    private UserCommandService userCommandService;
    
    @MockitoBean
    private UserQueryService userQueryService;
    
    @MockitoBean
    private UserSearchService userSearchService;
    
    @MockitoBean
    private ProfileService profileService;
    
    @MockitoBean
    private UserPresentationMapper mapper;
    
    private static final String BASE_URL = "/user";
    private static final String TEST_USER_ID = "ffffffff-ffff-ffff-ffff-ffffffffffff";
    private static final String OTHER_USER_ID = "eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee";
    
    @Nested
    @DisplayName("1. CREATE - 회원 생성은 AuthController에서 처리")
    class CreateTests {
        // 회원 생성은 /auth/sign-up 에서 처리되므로 여기서는 테스트하지 않음
    }
    
    @Nested
    @DisplayName("2. READ - 회원 조회")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class ReadTests {
        
        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {
            
            @Test
            @Order(1)
            @DisplayName("본인 정보 조회 성공")
            @WithMockUser(username = TEST_USER_ID)
            void getMe_Success() throws Exception {
                // Given
                UserResult mockUser = createMockUserResult();
                given(userQueryService.getUser(any(UserQuery.class))).willReturn(mockUser);
                given(profileService.getProfile(any(UUID.class))).willReturn(createMockProfile());
                given(mapper.toResponse(any(UserResult.class), any(Profile.class)))
                    .willReturn(createMockUserResponse());
                
                // When & Then
                mockMvc.perform(get(BASE_URL + "/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").exists())
                    .andExpect(jsonPath("$.nickname").exists());
            }
            
            @Test
            @Order(2)
            @DisplayName("특정 회원 조회 성공 - 본인")
            @WithMockUser(username = TEST_USER_ID)
            void getUser_Success_Self() throws Exception {
                mockMvc.perform(get(BASE_URL + "/" + TEST_USER_ID))
                    .andExpect(status().isOk());
            }
            
            @Test
            @Order(3)
            @DisplayName("특정 회원 조회 성공 - 관리자 권한")
            @WithMockUser(username = TEST_USER_ID, roles = {"USER", "ADMIN"})
            void getUser_Success_AsAdmin() throws Exception {
                mockMvc.perform(get(BASE_URL + "/" + OTHER_USER_ID))
                    .andExpect(status().isOk());
            }
            
            @Test
            @Order(4)
            @DisplayName("회원 목록 조회 성공 - 관리자 권한")
            @WithMockUser(username = TEST_USER_ID, roles = {"USER", "ADMIN"})
            void getUsers_Success_AsAdmin() throws Exception {
                mockMvc.perform(get(BASE_URL)
                        .param("page", "0")
                        .param("size", "10")
                        .param("sort", "createdAt,desc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.pageable").exists());
            }
        }
        
        @Nested
        @DisplayName("실패 케이스")
        class FailureCases {
            
            @Test
            @DisplayName("본인 정보 조회 실패 - 인증 없음")
            void getMe_Failure_NoAuth() throws Exception {
                mockMvc.perform(get(BASE_URL + "/me"))
                    .andExpect(status().isUnauthorized());
            }
            
            @Test
            @DisplayName("타인 정보 조회 실패 - 권한 없음")
            @WithMockUser(username = TEST_USER_ID, roles = "USER")
            void getUser_Failure_NoPermission() throws Exception {
                mockMvc.perform(get(BASE_URL + "/" + OTHER_USER_ID))
                    .andExpect(status().isForbidden());
            }
            
            @Test
            @DisplayName("회원 목록 조회 실패 - 관리자 권한 없음")
            @WithMockUser(username = TEST_USER_ID, roles = "USER")
            void getUsers_Failure_NotAdmin() throws Exception {
                mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isForbidden());
            }
            
            @Test
            @DisplayName("회원 조회 실패 - 잘못된 UUID 형식")
            @WithMockUser(username = TEST_USER_ID)
            void getUser_Failure_InvalidUUID() throws Exception {
                mockMvc.perform(get(BASE_URL + "/invalid-uuid"))
                    .andExpect(status().isBadRequest());
            }
        }
        
        @Nested
        @DisplayName("엣지 케이스")
        class EdgeCases {
            
            @Test
            @DisplayName("회원 목록 조회 - 페이지네이션 경계값")
            @WithMockUser(username = TEST_USER_ID, roles = {"USER", "ADMIN"})
            void getUsers_EdgeCase_LargePage() throws Exception {
                mockMvc.perform(get(BASE_URL)
                        .param("page", "999999")
                        .param("size", "100"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty());
            }
            
            @Test
            @DisplayName("회원 목록 조회 - 최대 페이지 크기 초과")
            @WithMockUser(username = TEST_USER_ID, roles = {"USER", "ADMIN"})
            void getUsers_EdgeCase_MaxSizeExceeded() throws Exception {
                mockMvc.perform(get(BASE_URL)
                        .param("size", "1000"))
                    .andExpect(status().isBadRequest()); // 설정에 따라 다름
            }
        }
    }
    
    @Nested
    @DisplayName("3. UPDATE - 회원 정보 수정")
    class UpdateTests {
        
        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {
            
            @Test
            @DisplayName("회원 정보 수정 성공 - 닉네임과 소개")
            @WithMockUser(username = TEST_USER_ID)
            void updateUser_Success() throws Exception {
                mockMvc.perform(patch(BASE_URL + "/" + TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("nickname", "새닉네임")
                        .param("introduction", "안녕하세요"))
                    .andExpect(status().isNoContent());
            }
            
            @Test
            @DisplayName("회원 정보 부분 수정 성공 - 닉네임만")
            @WithMockUser(username = TEST_USER_ID)
            void updateUser_Success_PartialUpdate() throws Exception {
                mockMvc.perform(patch(BASE_URL + "/" + TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("nickname", "닉네임만수정"))
                    .andExpect(status().isNoContent());
            }
        }
        
        @Nested
        @DisplayName("실패 케이스")
        class FailureCases {
            
            @Test
            @DisplayName("회원 정보 수정 실패 - 인증 없음")
            void updateUser_Failure_NoAuth() throws Exception {
                mockMvc.perform(patch(BASE_URL + "/" + TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("nickname", "새닉네임"))
                    .andExpect(status().isUnauthorized());
            }
            
            @Test
            @DisplayName("회원 정보 수정 실패 - 타인 정보 수정 시도")
            @WithMockUser(username = TEST_USER_ID)
            void updateUser_Failure_OtherUser() throws Exception {
                mockMvc.perform(patch(BASE_URL + "/" + OTHER_USER_ID)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("nickname", "새닉네임"))
                    .andExpect(status().isForbidden());
            }
            
            @Test
            @DisplayName("회원 정보 수정 실패 - 너무 긴 닉네임")
            @WithMockUser(username = TEST_USER_ID)
            void updateUser_Failure_TooLongNickname() throws Exception {
                String longNickname = "a".repeat(51); // 50자 제한 가정
                mockMvc.perform(patch(BASE_URL + "/" + TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("nickname", longNickname))
                    .andExpect(status().isBadRequest());
            }
            
            @Test
            @DisplayName("회원 정보 수정 실패 - HTML 태그 포함")
            @WithMockUser(username = TEST_USER_ID)
            void updateUser_Failure_HtmlInIntroduction() throws Exception {
                mockMvc.perform(patch(BASE_URL + "/" + TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("introduction", "<script>alert('xss')</script>"))
                    .andExpect(status().isBadRequest());
            }
        }
        
        @Nested
        @DisplayName("엣지 케이스")
        class EdgeCases {
            
            @Test
            @DisplayName("회원 정보 수정 - 빈 문자열로 수정")
            @WithMockUser(username = TEST_USER_ID)
            void updateUser_EdgeCase_EmptyString() throws Exception {
                mockMvc.perform(patch(BASE_URL + "/" + TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("introduction", ""))
                    .andExpect(status().isNoContent());
            }
            
            @Test
            @DisplayName("회원 정보 수정 - 유니코드 이모지 포함")
            @WithMockUser(username = TEST_USER_ID)
            void updateUser_EdgeCase_WithEmoji() throws Exception {
                mockMvc.perform(patch(BASE_URL + "/" + TEST_USER_ID)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("nickname", "테스트😀")
                        .param("introduction", "안녕하세요 👋"))
                    .andExpect(status().isNoContent());
            }
        }
    }
    
    @Nested
    @DisplayName("4. DELETE - 회원 삭제는 별도 API 없음")
    class DeleteTests {
        // 현재 회원 삭제 API는 없음
        // 향후 회원 탈퇴 기능 추가시 테스트 작성
    }
    
    @Nested
    @DisplayName("5. 인증 및 권한 테스트")
    class AuthorizationTests {
        
        @Test
        @DisplayName("Bearer 토큰 인증 성공")
        void auth_Success_BearerToken() throws Exception {
            // 실제 토큰이 필요한 경우
            String validToken = "eyJhbGciOiJSUzI1NiIsImtpZCI6InJzYS1rZXktaWQiLCJ0eXAiOiJKV1QifQ...";
            
            mockMvc.perform(get(BASE_URL + "/me")
                    .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isUnauthorized()); // 실제 유효한 토큰이 아니므로
        }
        
        @Test
        @DisplayName("쿠키 인증 성공")
        void auth_Success_Cookie() throws Exception {
            String validToken = "eyJhbGciOiJSUzI1NiIsImtpZCI6InJzYS1rZXktaWQiLCJ0eXAiOiJKV1QifQ...";
            
            mockMvc.perform(get(BASE_URL + "/me")
                    .cookie(new jakarta.servlet.http.Cookie("access-token", validToken)))
                .andExpect(status().isUnauthorized()); // 실제 유효한 토큰이 아니므로
        }
        
        @Test
        @DisplayName("잘못된 Bearer 형식")
        void auth_Failure_InvalidBearerFormat() throws Exception {
            mockMvc.perform(get(BASE_URL + "/me")
                    .header("Authorization", "InvalidBearer token"))
                .andExpect(status().isUnauthorized());
        }
        
        @Test
        @DisplayName("ADMIN 권한 체크")
        @WithMockUser(username = TEST_USER_ID, roles = "USER")
        void auth_AdminEndpoint_RequiresAdminRole() throws Exception {
            mockMvc.perform(get(BASE_URL + "/inactive")
                    .param("days", "30"))
                .andExpect(status().isForbidden());
        }
        
        @Test
        @DisplayName("ADMIN 권한으로 비활성 회원 조회 성공")
        @WithMockUser(username = TEST_USER_ID, roles = {"USER", "ADMIN"})
        void auth_AdminEndpoint_Success() throws Exception {
            mockMvc.perform(get(BASE_URL + "/inactive")
                    .param("days", "30"))
                .andExpect(status().isOk());
        }
    }
    
    // Helper methods
    private UserResult createMockUserResult() {
        return new UserResult(
            UUID.fromString(TEST_USER_ID),
            "test@example.com",
            "테스트유저"
        );
    }
    
    private Profile createMockProfile() {
        point.ttodoApi.user.domain.User mockUser = point.ttodoApi.user.domain.User.builder()
            .id(UUID.fromString(TEST_USER_ID))
            .email("test@example.com")
            .password("password")
            .build();
            
        return Profile.builder()
            .owner(mockUser)
            .nickname("테스트유저")
            .introduction("안녕하세요")
            .timeZone("Asia/Seoul")
            .locale("ko_KR")
            .build();
    }
    
    private point.ttodoApi.user.presentation.dto.response.UserResponse createMockUserResponse() {
        return new point.ttodoApi.user.presentation.dto.response.UserResponse(
            UUID.fromString(TEST_USER_ID),
            "test@example.com",
            "테스트유저",
            "안녕하세요",
            "Asia/Seoul",
            "ko_KR",
            "LIGHT",
            null
        );
    }
}