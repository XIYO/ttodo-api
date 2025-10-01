package point.ttodoApi.challenge.presentation;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import point.ttodoApi.challenge.application.*;
import point.ttodoApi.challenge.presentation.mapper.ChallengePresentationMapper;
import point.ttodoApi.shared.config.auth.ApiSecurityTestConfig;
import point.ttodoApi.shared.error.*;
import point.ttodoApi.user.application.UserService;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * ChallengeController 단위 테스트
 * 개별 Mock 방식 적용
 * @WithMockUser 기반 인증 처리
 * CRUD 순서 + Nested 구조 + 한글 DisplayName
 */
@WebMvcTest(ChallengeController.class)
@Import(ApiSecurityTestConfig.class)
@DisplayName("ChallengeController 단위 테스트")
@Tag("unit")
@Tag("challenge")
@Tag("controller")
class ChallengeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ChallengeService challengeService;

    @MockitoBean
    private ChallengeSearchService challengeSearchService;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private ChallengePresentationMapper challengePresentationMapper;

    @MockitoBean
    private ErrorMetricsCollector errorMetricsCollector;

    private static final String BASE_URL = "/challenges";
    private static final String TEST_USER_ID = "ffffffff-ffff-ffff-ffff-ffffffffffff";

    @BeforeEach
    void setUp() {
        // 기본 성공 응답 설정 - 간소화
        given(challengeService.createChallenge(any())).willReturn(1L);
        org.mockito.Mockito.doNothing().when(challengeService).deleteChallenge(anyLong());
    }

    @Nested
    @DisplayName("1. CREATE - 챌린지 생성")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class CreateTests {
        
        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {
            
            @Test
            @Order(1)
            @DisplayName("챌린지 생성 성공 - 유효한 데이터")
            @WithMockUser(username = TEST_USER_ID, roles = "USER")
            void createChallenge_Success_WithValidData() throws Exception {
                mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "Test Challenge")
                        .param("description", "Test Description")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-01-31"))
                    .andExpect(status().isCreated());
            }
        }
        
        @Nested
        @DisplayName("실패 케이스 - 인증")
        class AuthFailureCases {
            
            @Test
            @DisplayName("챌린지 생성 실패 - 인증 없음")
            void createChallenge_Failure_WithoutAuth() throws Exception {
                mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "Test Challenge")
                        .param("description", "Test Description")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-01-31"))
                    .andExpect(status().isForbidden());
            }
        }
        
        @Nested
        @DisplayName("실패 케이스 - 일반")
        class GeneralFailureCases {
            
            @Test
            @DisplayName("챌린지 생성 실패 - 제목 미입력")
            @WithMockUser(username = TEST_USER_ID, roles = "USER")
            void createChallenge_Failure_NoTitle() throws Exception {
                mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("description", "Test Description")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-01-31"))
                    .andExpect(status().isBadRequest());
            }
        }
        
        @Nested
        @DisplayName("엣지 케이스")
        class EdgeCases {
            
            @Test
            @DisplayName("챌린지 생성 - 설명 미입력시 기본값 사용")
            @WithMockUser(username = TEST_USER_ID, roles = "USER")
            void createChallenge_EdgeCase_NoDescription() throws Exception {
                mockMvc.perform(post(BASE_URL)
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "Test Challenge")
                        .param("startDate", "2025-01-01")
                        .param("endDate", "2025-01-31"))
                    .andExpect(status().isCreated());
            }
        }
    }

    @Nested
    @DisplayName("2. READ - 챌린지 조회")
    class ReadTests {
        
        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {
            
            @Test
            @DisplayName("챌린지 목록 조회 성공")
            @WithMockUser(username = TEST_USER_ID, roles = "USER")
            void getChallenges_Success() throws Exception {
                mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk());
            }
            
            @Test
            @DisplayName("챌린지 상세 조회 성공")
            @WithMockUser(username = TEST_USER_ID, roles = "USER")
            void getChallenge_Success() throws Exception {
                mockMvc.perform(get(BASE_URL + "/1"))
                    .andExpect(status().isOk());
            }
        }
        
        @Nested
        @DisplayName("실패 케이스 - 인증")
        class AuthFailureCases {
            
            @Test
            @DisplayName("챌린지 목록 조회 실패 - 인증 없음")
            void getChallenges_Failure_WithoutAuth() throws Exception {
                mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isForbidden());
            }
            
            @Test
            @DisplayName("챌린지 상세 조회 실패 - 인증 없음")
            void getChallenge_Failure_WithoutAuth() throws Exception {
                mockMvc.perform(get(BASE_URL + "/1"))
                    .andExpect(status().isForbidden());
            }
        }
        
        @Nested
        @DisplayName("실패 케이스 - 일반")
        class GeneralFailureCases {
            
            @Test
            @DisplayName("챌린지 상세 조회 실패 - 존재하지 않는 챌린지")
            @WithMockUser(username = TEST_USER_ID, roles = "USER")
            void getChallenge_Failure_NotFound() throws Exception {
                // 간소화된 테스트 - 실제 서비스 메서드 확인 후 수정 필요
                mockMvc.perform(get(BASE_URL + "/999"))
                    .andExpect(status().isNotFound());
            }
        }
    }

    @Nested
    @DisplayName("3. UPDATE - 챌린지 수정")
    class UpdateTests {
        
        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {
            
            @Test
            @DisplayName("챌린지 수정 성공")
            @WithMockUser(username = TEST_USER_ID, roles = "USER")
            void updateChallenge_Success() throws Exception {
                mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "Updated Challenge")
                        .param("description", "Updated Description"))
                    .andExpect(status().isOk());
            }
        }
        
        @Nested
        @DisplayName("실패 케이스 - 인증")
        class AuthFailureCases {
            
            @Test
            @DisplayName("챌린지 수정 실패 - 인증 없음")
            void updateChallenge_Failure_WithoutAuth() throws Exception {
                mockMvc.perform(put(BASE_URL + "/1")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "Updated Challenge"))
                    .andExpect(status().isForbidden());
            }
        }
    }

    @Nested
    @DisplayName("4. DELETE - 챌린지 삭제")
    class DeleteTests {
        
        @Nested
        @DisplayName("성공 케이스")
        class SuccessCases {
            
            @Test
            @DisplayName("챌린지 삭제 성공")
            @WithMockUser(username = TEST_USER_ID, roles = "USER")
            void deleteChallenge_Success() throws Exception {
                mockMvc.perform(delete(BASE_URL + "/1"))
                    .andExpect(status().isNoContent());
            }
        }
        
        @Nested
        @DisplayName("실패 케이스 - 인증")
        class AuthFailureCases {
            
            @Test
            @DisplayName("챌린지 삭제 실패 - 인증 없음")
            void deleteChallenge_Failure_WithoutAuth() throws Exception {
                mockMvc.perform(delete(BASE_URL + "/1"))
                    .andExpect(status().isForbidden());
            }
        }
    }

}