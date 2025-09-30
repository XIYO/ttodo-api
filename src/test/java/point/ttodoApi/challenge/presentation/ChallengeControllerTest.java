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
import point.ttodoApi.challenge.application.command.*;
import point.ttodoApi.challenge.application.result.ChallengeResult;
import point.ttodoApi.challenge.presentation.dto.response.ChallengeResponse;
import point.ttodoApi.challenge.presentation.mapper.ChallengePresentationMapper;
import point.ttodoApi.shared.config.auth.ApiSecurityTestConfig;
import point.ttodoApi.shared.error.*;
import point.ttodoApi.user.application.UserService;
import point.ttodoApi.user.domain.User;

import java.time.LocalDate;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

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

    private static final UUID TEST_USER_ID = UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff");
    private static final String TEST_TITLE = "Test Challenge";
    private static final String TEST_DESCRIPTION = "Test Description";
    private static final LocalDate TEST_START_DATE = LocalDate.of(2025, 1, 1);
    private static final LocalDate TEST_END_DATE = LocalDate.of(2025, 1, 31);

    @BeforeEach
    void setUp() {
        User mockUser = User.builder()
            .id(TEST_USER_ID)
            .email("test@example.com")
            .build();
        
        given(userService.findVerifiedUser(any(UUID.class))).willReturn(mockUser);
        given(challengePresentationMapper.toCommand(any(), any()))
            .willReturn(new CreateChallengeCommand(
                TEST_USER_ID,
                TEST_TITLE,
                TEST_DESCRIPTION,
                TEST_START_DATE,
                TEST_END_DATE,
                null,
                true
            ));
        
        given(challengeService.createChallenge(any())).willReturn(1L);
        
        ChallengeResult mockResult = new ChallengeResult(
            1L,
            TEST_TITLE,
            TEST_DESCRIPTION,
            TEST_START_DATE,
            TEST_END_DATE,
            TEST_USER_ID,
            0,
            null,
            true,
            null
        );
        given(challengeService.getChallengeDetailForPublic(anyLong())).willReturn(mockResult);
        
        ChallengeResponse mockResponse = new ChallengeResponse(
            1L,
            TEST_TITLE,
            TEST_DESCRIPTION,
            TEST_START_DATE,
            TEST_END_DATE,
            TEST_USER_ID,
            0,
            null,
            true
        );
        given(challengePresentationMapper.toChallengeResponse(any())).willReturn(mockResponse);
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
            @WithMockUser
            void createChallenge_Success_WithValidData() throws Exception {
                mockMvc.perform(post("/challenges")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", TEST_TITLE)
                        .param("description", TEST_DESCRIPTION)
                        .param("startDate", TEST_START_DATE.toString())
                        .param("endDate", TEST_END_DATE.toString()))
                    .andExpect(status().isCreated());
            }
        }
        
        @Nested
        @DisplayName("실패 케이스 - 인증")
        class AuthFailureCases {
            
            @Test
            @DisplayName("챌린지 생성 실패 - 인증 없음")
            void createChallenge_Failure_WithoutAuth() throws Exception {
                mockMvc.perform(post("/challenges")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", TEST_TITLE)
                        .param("description", TEST_DESCRIPTION)
                        .param("startDate", TEST_START_DATE.toString())
                        .param("endDate", TEST_END_DATE.toString()))
                    .andExpect(status().isForbidden());
            }
        }
        
        @Nested
        @DisplayName("실패 케이스 - 일반")
        class GeneralFailureCases {
            
            @Test
            @DisplayName("챌린지 생성 실패 - 제목 미입력")
            @WithMockUser
            void createChallenge_Failure_WithoutTitle() throws Exception {
                mockMvc.perform(post("/challenges")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("description", TEST_DESCRIPTION)
                        .param("startDate", TEST_START_DATE.toString())
                        .param("endDate", TEST_END_DATE.toString()))
                    .andExpect(status().isBadRequest());
            }
            
            @Test
            @DisplayName("챌린지 생성 실패 - 종료일이 시작일보다 빠름")
            @WithMockUser
            void createChallenge_Failure_EndDateBeforeStartDate() throws Exception {
                given(challengeService.createChallenge(any()))
                    .willThrow(new BusinessException(ErrorCode.INVALID_OPERATION));
                
                mockMvc.perform(post("/challenges")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", TEST_TITLE)
                        .param("description", TEST_DESCRIPTION)
                        .param("startDate", "2025-01-31")
                        .param("endDate", "2025-01-01"))
                    .andExpect(status().isBadRequest());
            }
        }
        
        @Nested
        @DisplayName("엣지 케이스")
        class EdgeCases {
            
            @Test
            @DisplayName("챌린지 생성 - 선택 필드 미입력")
            @WithMockUser
            void createChallenge_EdgeCase_WithoutOptionalFields() throws Exception {
                mockMvc.perform(post("/challenges")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", TEST_TITLE)
                        .param("description", TEST_DESCRIPTION)
                        .param("startDate", TEST_START_DATE.toString())
                        .param("endDate", TEST_END_DATE.toString()))
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
            @WithMockUser
            void getChallenges_Success() throws Exception {
                mockMvc.perform(get("/challenges")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isOk());
            }
            
            @Test
            @DisplayName("챌린지 상세 조회 성공")
            @WithMockUser
            void getChallenge_Success() throws Exception {
                mockMvc.perform(get("/challenges/1")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isOk());
            }
        }
        
        @Nested
        @DisplayName("실패 케이스 - 인증")
        class AuthFailureCases {
            
            @Test
            @DisplayName("챌린지 목록 조회 실패 - 인증 없음")
            void getChallenges_Failure_WithoutAuth() throws Exception {
                mockMvc.perform(get("/challenges")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isForbidden());
            }
        }
        
        @Nested
        @DisplayName("실패 케이스 - 일반")
        class GeneralFailureCases {
            
            @Test
            @DisplayName("챌린지 상세 조회 실패 - 존재하지 않는 챌린지")
            @WithMockUser
            void getChallenge_Failure_NotFound() throws Exception {
                given(challengeService.getChallengeDetailForPublic(anyLong()))
                    .willThrow(new BusinessException(ErrorCode.ENTITY_NOT_FOUND));
                
                mockMvc.perform(get("/challenges/999")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
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
            @WithMockUser
            void updateChallenge_Success() throws Exception {
                org.mockito.Mockito.doNothing().when(challengeService).updateChallenge(anyLong(), any());
                
                mockMvc.perform(put("/challenges/1")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "Updated Title")
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
                mockMvc.perform(put("/challenges/1")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "Updated Title"))
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
            @WithMockUser
            void deleteChallenge_Success() throws Exception {
                org.mockito.Mockito.doNothing().when(challengeService).deleteChallenge(anyLong());
                
                mockMvc.perform(delete("/challenges/1")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isNoContent());
            }
        }
        
        @Nested
        @DisplayName("실패 케이스 - 인증")
        class AuthFailureCases {
            
            @Test
            @DisplayName("챌린지 삭제 실패 - 인증 없음")
            void deleteChallenge_Failure_WithoutAuth() throws Exception {
                mockMvc.perform(delete("/challenges/1")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isForbidden());
            }
        }
        
        @Nested
        @DisplayName("실패 케이스 - 일반")
        class GeneralFailureCases {
            
            @Test
            @DisplayName("챌린지 삭제 실패 - 존재하지 않는 챌린지")
            @WithMockUser
            void deleteChallenge_Failure_NotFound() throws Exception {
                org.mockito.Mockito.doThrow(new BusinessException(ErrorCode.ENTITY_NOT_FOUND))
                    .when(challengeService).deleteChallenge(anyLong());
                
                mockMvc.perform(delete("/challenges/999")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                    .andExpect(status().isNotFound());
            }
        }
    }
}
