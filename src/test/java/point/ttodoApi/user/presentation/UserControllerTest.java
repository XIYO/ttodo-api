package point.ttodoApi.user.presentation;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import point.ttodoApi.profile.application.ProfileService;
import point.ttodoApi.shared.config.auth.ApiSecurityTestConfig;
import point.ttodoApi.shared.error.ErrorMetricsCollector;
import point.ttodoApi.user.application.*;
import point.ttodoApi.user.application.result.UserResult;
import point.ttodoApi.user.presentation.mapper.UserPresentationMapper;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * UserController 단위 테스트
 * 개별 Mock 방식 적용
 * @WithMockUser 기반 인증 처리
 * CRUD 순서 + Nested 구조 + 한글 DisplayName
 */
@WebMvcTest(UserController.class)
@Import(ApiSecurityTestConfig.class)
@DisplayName("UserController 단위 테스트")
@Tag("unit")
@Tag("user")
@Tag("controller")
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;
    
    @MockitoBean
    private UserCommandService userCommandService;
    
    @MockitoBean
    private UserQueryService userQueryService;
    
    @MockitoBean
    private ProfileService profileService;
    
    @MockitoBean
    private UserPresentationMapper userPresentationMapper;
    
    @MockitoBean
    private ErrorMetricsCollector errorMetricsCollector;

    private static final String BASE_URL = "/users";
    private static final String TEST_USER_ID = "ffffffff-ffff-ffff-ffff-ffffffffffff";

    @BeforeEach
    void setUp() {
        // 기본 성공 응답 설정 - 간소화
        UserResult mockResult = new UserResult(
                UUID.fromString(TEST_USER_ID),
                "test@example.com",
                "Test User"
        );
        given(userCommandService.updateUser(any())).willReturn(mockResult);
        // deleteUser 메서드는 실제 구현 확인 후 수정 필요
    }

    @Nested
    @DisplayName("1. READ - 사용자 조회")
    class ReadTests {
        
        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {
            
            @Test
            @DisplayName("사용자 정보 조회 성공")
            @WithMockUser(username = TEST_USER_ID, roles = "USER")
            void getUser_Success() throws Exception {
                mockMvc.perform(get(BASE_URL + "/me"))
                    .andExpect(status().isOk());
            }
        }
        
        @Nested
        @DisplayName("실패 케이스 - 인증")
        class AuthFailureCases {
            
            @Test
            @DisplayName("사용자 정보 조회 실패 - 인증 없음")
            void getUser_Failure_WithoutAuth() throws Exception {
                mockMvc.perform(get(BASE_URL + "/me"))
                    .andExpect(status().isForbidden());
            }
        }
    }

    @Nested
    @DisplayName("2. UPDATE - 사용자 수정")
    class UpdateTests {
        
        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {
            
            @Test
            @DisplayName("사용자 정보 수정 성공")
            @WithMockUser(username = TEST_USER_ID, roles = "USER")
            void updateUser_Success() throws Exception {
                mockMvc.perform(put(BASE_URL + "/me")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("nickname", "Updated User"))
                    .andExpect(status().isOk());
            }
        }
        
        @Nested
        @DisplayName("실패 케이스 - 인증")
        class AuthFailureCases {
            
            @Test
            @DisplayName("사용자 정보 수정 실패 - 인증 없음")
            void updateUser_Failure_WithoutAuth() throws Exception {
                mockMvc.perform(put(BASE_URL + "/me")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("nickname", "Updated User"))
                    .andExpect(status().isForbidden());
            }
        }
    }

    @Nested
    @DisplayName("3. DELETE - 사용자 삭제")
    class DeleteTests {
        
        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {
            
            @Test
            @DisplayName("사용자 삭제 성공")
            @WithMockUser(username = TEST_USER_ID, roles = "USER")
            void deleteUser_Success() throws Exception {
                mockMvc.perform(delete(BASE_URL + "/me"))
                    .andExpect(status().isNoContent());
            }
        }
        
        @Nested
        @DisplayName("실패 케이스 - 인증")
        class AuthFailureCases {
            
            @Test
            @DisplayName("사용자 삭제 실패 - 인증 없음")
            void deleteUser_Failure_WithoutAuth() throws Exception {
                mockMvc.perform(delete(BASE_URL + "/me"))
                    .andExpect(status().isForbidden());
            }
        }
    }

}