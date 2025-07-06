package point.zzicback.challenge.presentation;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import point.zzicback.test.config.TestSecurityConfig;
import point.zzicback.challenge.domain.Challenge;
import point.zzicback.challenge.domain.PeriodType;
import point.zzicback.challenge.infrastructure.ChallengeRepository;
import point.zzicback.member.application.MemberService;
import point.zzicback.member.application.dto.command.CreateMemberCommand;
import point.zzicback.member.domain.Member;
import point.zzicback.member.infrastructure.persistence.MemberRepository;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@Import(TestSecurityConfig.class)
@DisplayName("챌린지 엣지 케이스 테스트")
class ChallengeEdgeCaseTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberService memberService;

    private Member testMember;

    @BeforeEach
    void setUp() {
        testMember = memberRepository.save(Member.builder()
                .email("edge@example.com")
                .nickname("엣지테스터")
                .build());
    }

    @Nested
    @DisplayName("경계값 테스트")
    class BoundaryValueTest {

        @Test
        @DisplayName("최대 제목 길이 (255자)")
        @WithUserDetails("anon@zzic.com")
        void createChallenge_MaxTitleLength() throws Exception {
            String maxTitle = "a".repeat(255);
            
            mockMvc.perform(post("/challenges")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("title", maxTitle)
                    .param("periodType", "DAILY")
                    .param("visibility", "PUBLIC")
                    .param("startDate", LocalDate.now().toString())
                    .param("endDate", LocalDate.now().plusDays(7).toString()))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("제목 길이 초과 (256자)")
        @WithUserDetails("anon@zzic.com")
        void createChallenge_ExceedMaxTitleLength() throws Exception {
            String overTitle = "a".repeat(256);
            
            mockMvc.perform(post("/challenges")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("title", overTitle)
                    .param("periodType", "DAILY")
                    .param("visibility", "PUBLIC")
                    .param("startDate", LocalDate.now().toString())
                    .param("endDate", LocalDate.now().plusDays(7).toString()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("최대 설명 길이 (65535자)")
        @WithUserDetails("anon@zzic.com")
        void createChallenge_MaxDescriptionLength() throws Exception {
            String maxDescription = "b".repeat(65535);
            
            mockMvc.perform(post("/challenges")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("title", "설명 테스트")
                    .param("description", maxDescription)
                    .param("periodType", "DAILY")
                    .param("visibility", "PUBLIC")
                    .param("startDate", LocalDate.now().toString())
                    .param("endDate", LocalDate.now().plusDays(7).toString()))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("1명 최대 참여 인원")
        @WithUserDetails("anon@zzic.com")
        void createChallenge_MinParticipants() throws Exception {
            // given
            ResultActions result = mockMvc.perform(post("/challenges")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("title", "1인 챌린지")
                    .param("periodType", "DAILY")
                    .param("visibility", "PUBLIC")
                    .param("startDate", LocalDate.now().toString())
                    .param("endDate", LocalDate.now().plusDays(7).toString())
                    .param("maxParticipants", "1"))
                    .andExpect(status().isCreated());
            
            Long challengeId = extractChallengeId(result);
            
            // when & then - 첫 번째 참여 성공
            mockMvc.perform(post("/challenges/{challengeId}/participants", challengeId))
                    .andExpect(status().isCreated());
            
            // 두 번째 참여 실패 - 새로운 사용자 생성
            CreateMemberCommand anotherCommand = new CreateMemberCommand(
                "another@example.com",
                "password",
                "다른유저",
                null
            );
            Member otherMember = memberService.createMember(anotherCommand);
            
            mockMvc.perform(post("/challenges/{challengeId}/participants", challengeId)
                    .with(user(otherMember.getEmail())))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("1000명 최대 참여 인원")
        @WithUserDetails("anon@zzic.com")
        void createChallenge_MaxParticipants() throws Exception {
            mockMvc.perform(post("/challenges")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("title", "대규모 챌린지")
                    .param("periodType", "MONTHLY")
                    .param("visibility", "PUBLIC")
                    .param("startDate", LocalDate.now().toString())
                    .param("endDate", LocalDate.now().plusMonths(3).toString())
                    .param("maxParticipants", "1000"))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("최대 참여 인원 초과 (1001명)")
        @WithUserDetails("anon@zzic.com")
        void createChallenge_ExceedMaxParticipants() throws Exception {
            mockMvc.perform(post("/challenges")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("title", "초과 인원")
                    .param("periodType", "DAILY")
                    .param("visibility", "PUBLIC")
                    .param("startDate", LocalDate.now().toString())
                    .param("endDate", LocalDate.now().plusDays(7).toString())
                    .param("maxParticipants", "1001"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("1일 기간 챌린지")
        @WithUserDetails("anon@zzic.com")
        void createChallenge_OneDayDuration() throws Exception {
            LocalDate today = LocalDate.now();
            LocalDate tomorrow = today.plusDays(1);
            
            mockMvc.perform(post("/challenges")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("title", "하루 챌린지")
                    .param("periodType", "DAILY")
                    .param("visibility", "PUBLIC")
                    .param("startDate", today.toString())
                    .param("endDate", tomorrow.toString()))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("365일 기간 챌린지")
        @WithUserDetails("anon@zzic.com")
        void createChallenge_OneYearDuration() throws Exception {
            LocalDate startDate = LocalDate.now();
            LocalDate endDate = startDate.plusDays(365);
            
            mockMvc.perform(post("/challenges")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("title", "1년 챌린지")
                    .param("periodType", "MONTHLY")
                    .param("visibility", "PUBLIC")
                    .param("startDate", startDate.toString())
                    .param("endDate", endDate.toString()))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("0명 최대 참여 인원")
        @WithUserDetails("anon@zzic.com")
        void createChallenge_ZeroParticipants() throws Exception {
            mockMvc.perform(post("/challenges")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("title", "참여 불가 챌린지")
                    .param("periodType", "DAILY")
                    .param("visibility", "PUBLIC")
                    .param("startDate", LocalDate.now().toString())
                    .param("endDate", LocalDate.now().plusDays(7).toString())
                    .param("maxParticipants", "0"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("음수 최대 참여 인원")
        @WithUserDetails("anon@zzic.com")
        void createChallenge_NegativeParticipants() throws Exception {
            mockMvc.perform(post("/challenges")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("title", "음수 인원")
                    .param("periodType", "DAILY")
                    .param("visibility", "PUBLIC")
                    .param("startDate", LocalDate.now().toString())
                    .param("endDate", LocalDate.now().plusDays(7).toString())
                    .param("maxParticipants", "-10"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("특수 케이스 테스트")
    class SpecialCaseTest {

        @Test
        @DisplayName("초대 코드 중복 방지")
        @WithUserDetails("anon@zzic.com")
        void createChallenge_UniqueInviteCode() throws Exception {
            // given - 100개의 초대 전용 챌린지 생성
            for (int i = 0; i < 100; i++) {
                mockMvc.perform(post("/challenges")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("title", "초대 챌린지 " + i)
                        .param("periodType", "DAILY")
                        .param("visibility", "INVITE_ONLY")
                        .param("startDate", LocalDate.now().toString())
                        .param("endDate", LocalDate.now().plusDays(7).toString()))
                        .andExpect(status().isCreated());
            }
            
            // then - 모든 초대 코드가 고유한지 확인
            var challenges = challengeRepository.findAll();
            var inviteCodes = challenges.stream()
                    .map(Challenge::getInviteCode)
                    .filter(code -> code != null)
                    .distinct()
                    .count();
            
            assert inviteCodes == 100;
        }

        @Test
        @DisplayName("삭제된 후 재참여")
        @WithUserDetails("anon@zzic.com")
        void leaveAndRejoinChallenge() throws Exception {
            // given - Create challenge via API
            String challengeJson = mockMvc.perform(post("/challenges")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("title", "재참여 테스트")
                    .param("description", "설명")
                    .param("periodType", "DAILY")
                    .param("visibility", "PUBLIC")
                    .param("startDate", LocalDate.now().toString())
                    .param("endDate", LocalDate.now().plusDays(7).toString()))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();
            
            Long challengeId = extractChallengeId(challengeJson);
            
            // 참여
            mockMvc.perform(post("/challenges/{challengeId}/participants", challengeId))
                    .andExpect(status().isCreated());
            
            // 탈퇴
            mockMvc.perform(delete("/challenges/{challengeId}/participants", challengeId))
                    .andExpect(status().isNoContent());
            
            // 재참여
            mockMvc.perform(post("/challenges/{challengeId}/participants", challengeId))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("시작 전 챌린지 수정")
        @WithUserDetails("anon@zzic.com")
        void updateChallenge_BeforeStart() throws Exception {
            // given - Create challenge via API
            String challengeJson = mockMvc.perform(post("/challenges")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("title", "수정 가능")
                    .param("description", "원래 설명")
                    .param("periodType", "DAILY")
                    .param("visibility", "PUBLIC")
                    .param("startDate", LocalDate.now().plusDays(3).toString())
                    .param("endDate", LocalDate.now().plusDays(10).toString())
                    .param("maxParticipants", "50"))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();
            
            Long challengeId = extractChallengeId(challengeJson);
            
            // when & then
            mockMvc.perform(patch("/challenges/{challengeId}", challengeId)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("title", "수정된 제목")
                    .param("description", "수정된 설명")
                    .param("maxParticipants", "100"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("진행 중 챌린지의 최대 인원 축소 시도")
        @WithUserDetails("anon@zzic.com")
        void updateChallenge_ReduceMaxParticipants() throws Exception {
            // given - Create challenge via API
            String challengeJson = mockMvc.perform(post("/challenges")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("title", "인원 축소")
                    .param("description", "설명")
                    .param("periodType", "DAILY")
                    .param("visibility", "PUBLIC")
                    .param("startDate", LocalDate.now().toString())
                    .param("endDate", LocalDate.now().plusDays(7).toString())
                    .param("maxParticipants", "10"))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();
            
            Long challengeId = extractChallengeId(challengeJson);
            
            // 5명 참여시키기
            for (int i = 0; i < 5; i++) {
                Member member = memberRepository.save(Member.builder()
                        .email("user" + i + "@example.com")
                        .nickname("유저" + i)
                        .build());
                
                mockMvc.perform(post("/challenges/{challengeId}/participants", challengeId)
                        .with(user(member.getEmail())))
                        .andExpect(status().isCreated());
            }
            
            // when & then - 3명으로 축소 시도
            mockMvc.perform(patch("/challenges/{challengeId}", challengeId)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("maxParticipants", "3"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("빈 문자열 제목")
        @WithUserDetails("anon@zzic.com")
        void createChallenge_EmptyTitle() throws Exception {
            mockMvc.perform(post("/challenges")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("title", "")
                    .param("periodType", "DAILY")
                    .param("visibility", "PUBLIC")
                    .param("startDate", LocalDate.now().toString())
                    .param("endDate", LocalDate.now().plusDays(7).toString()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.title").exists());
        }

        @Test
        @DisplayName("공백만 있는 제목")
        @WithUserDetails("anon@zzic.com")
        void createChallenge_WhitespaceTitle() throws Exception {
            mockMvc.perform(post("/challenges")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("title", "   ")
                    .param("periodType", "DAILY")
                    .param("visibility", "PUBLIC")
                    .param("startDate", LocalDate.now().toString())
                    .param("endDate", LocalDate.now().plusDays(7).toString()))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("특수 문자가 포함된 제목")
        @WithUserDetails("anon@zzic.com")
        void createChallenge_SpecialCharacterTitle() throws Exception {
            mockMvc.perform(post("/challenges")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("title", "🚀 이모지 & 특수문자 @#$%")
                    .param("periodType", "DAILY")
                    .param("visibility", "PUBLIC")
                    .param("startDate", LocalDate.now().toString())
                    .param("endDate", LocalDate.now().plusDays(7).toString()))
                    .andExpect(status().isCreated());
        }
    }

    @Nested
    @DisplayName("페이지네이션 엣지 케이스")
    class PaginationEdgeCaseTest {

        @Test
        @DisplayName("음수 페이지 번호")
        void getChallenges_NegativePage() throws Exception {
            mockMvc.perform(get("/challenges")
                    .param("page", "-1"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("매우 큰 페이지 번호")
        void getChallenges_LargePage() throws Exception {
            mockMvc.perform(get("/challenges")
                    .param("page", "999999"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isEmpty());
        }

        @Test
        @DisplayName("0 크기 페이지")
        void getChallenges_ZeroSize() throws Exception {
            mockMvc.perform(get("/challenges")
                    .param("size", "0"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("최대 크기 초과")
        void getChallenges_ExceedMaxSize() throws Exception {
            mockMvc.perform(get("/challenges")
                    .param("size", "101"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("잘못된 정렬 필드")
        void getChallenges_InvalidSortField() throws Exception {
            mockMvc.perform(get("/challenges")
                    .param("sort", "invalidField,desc"))
                    .andExpect(status().isBadRequest());
        }
    }

    private Long extractChallengeId(String response) throws Exception {
        // Extract challengeId from JSON response
        return Long.parseLong(response.replaceAll(".*\"challengeId\":(\\d+).*", "$1"));
    }
    
    private Long extractChallengeId(ResultActions result) throws Exception {
        String response = result.andReturn().getResponse().getContentAsString();
        return extractChallengeId(response);
    }
}