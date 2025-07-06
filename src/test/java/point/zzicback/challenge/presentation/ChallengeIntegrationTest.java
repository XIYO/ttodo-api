package point.zzicback.challenge.presentation;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import point.zzicback.challenge.domain.Challenge;
import point.zzicback.challenge.domain.ChallengeVisibility;
import point.zzicback.challenge.domain.PeriodType;
import point.zzicback.challenge.infrastructure.ChallengeRepository;
import point.zzicback.member.application.MemberService;
import point.zzicback.member.application.dto.command.CreateMemberCommand;
import point.zzicback.member.domain.Member;
import point.zzicback.member.infrastructure.persistence.MemberRepository;

import java.time.LocalDate;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Import(point.zzicback.test.config.TestSecurityConfig.class)
class ChallengeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private MemberService memberService;

    private Member testMember;
    private Member otherMember;

    @BeforeEach
    void setUp() {
        // anon@zzic.com 사용자를 테스트 멤버로 사용
        testMember = memberService.findByEmail("anon@zzic.com")
                .orElseGet(() -> {
                    // 만약 없다면 생성
                    CreateMemberCommand command = new CreateMemberCommand(
                        "anon@zzic.com", 
                        "password", 
                        "익명의 찍찍이", 
                        "안녕하세요"
                    );
                    return memberService.createMember(command);
                });

        // 또 다른 테스트 회원 생성
        CreateMemberCommand anotherCommand = new CreateMemberCommand(
            "other@example.com",
            "password",
            "다른유저",
            null
        );
        otherMember = memberService.createMember(anotherCommand);
    }

    @Nested
    @DisplayName("챌린지 생성 테스트")
    class CreateChallengeTest {

        @Test
        @DisplayName("공개 챌린지 정상 생성")
        @WithUserDetails("anon@zzic.com")
        void createPublicChallenge_Success() throws Exception {
            // given
            Map<String, Object> request = Map.of(
                "title", "매일 운동하기",
                "description", "하루 30분 이상 운동",
                "periodType", "DAILY",
                "visibility", "PUBLIC",
                "startDate", LocalDate.now().plusDays(1).toString(),
                "endDate", LocalDate.now().plusMonths(1).toString(),
                "maxParticipants", 50
            );

            // when & then
            mockMvc.perform(post("/challenges")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("title", "매일 운동하기")
                    .param("description", "하루 30분 이상 운동")
                    .param("periodType", "DAILY")
                    .param("visibility", "PUBLIC")
                    .param("startDate", LocalDate.now().plusDays(1).toString())
                    .param("endDate", LocalDate.now().plusMonths(1).toString())
                    .param("maxParticipants", "50"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.challengeId").exists());
        }

        @Test
        @DisplayName("초대 전용 챌린지 생성 시 초대 코드 자동 생성")
        @WithUserDetails("anon@zzic.com")
        void createInviteOnlyChallenge_GeneratesInviteCode() throws Exception {
            // when
            ResultActions result = mockMvc.perform(post("/challenges")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("title", "비밀 챌린지")
                    .param("description", "초대받은 사람만")
                    .param("periodType", "WEEKLY")
                    .param("visibility", "INVITE_ONLY")
                    .param("startDate", LocalDate.now().toString())
                    .param("endDate", LocalDate.now().plusWeeks(4).toString()))
                    .andExpect(status().isCreated());
            
            // then
            String response = result.andReturn().getResponse().getContentAsString();
            Long challengeId = objectMapper.readValue(response, Map.class)
                    .get("challengeId")
                    .toString()
                    .transform(Long::parseLong);
            
            Challenge challenge = challengeRepository.findById(challengeId).orElseThrow();
            assert challenge.getInviteCode() != null;
            assert challenge.getInviteCode().length() == 8;
        }

        @Test
        @DisplayName("인증되지 않은 사용자의 챌린지 생성 시도")
        void createChallenge_Unauthorized() throws Exception {
            mockMvc.perform(post("/challenges")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("title", "인증 실패 챌린지")
                    .param("periodType", "DAILY")
                    .param("visibility", "PUBLIC")
                    .param("startDate", LocalDate.now().toString())
                    .param("endDate", LocalDate.now().plusDays(7).toString()))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("시작일이 종료일보다 늦은 경우")
        @WithUserDetails("anon@zzic.com")
        void createChallenge_InvalidDateRange() throws Exception {
            mockMvc.perform(post("/challenges")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("title", "잘못된 날짜 챌린지")
                    .param("periodType", "DAILY")
                    .param("visibility", "PUBLIC")
                    .param("startDate", LocalDate.now().plusDays(10).toString())
                    .param("endDate", LocalDate.now().plusDays(5).toString()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("요청 처리 실패"));
        }

        @Test
        @DisplayName("필수 필드 누락")
        @WithUserDetails("anon@zzic.com")
        void createChallenge_MissingRequiredFields() throws Exception {
            mockMvc.perform(post("/challenges")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .param("description", "제목이 없는 챌린지"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("입력값 검증 실패"))
                    .andExpect(jsonPath("$.errors.title").exists());
        }
    }

    @Nested
    @DisplayName("챌린지 조회 테스트")
    class GetChallengeTest {

        @Test
        @DisplayName("공개 챌린지만 목록에 노출")
        void getChallenges_OnlyPublic() throws Exception {
            // given
            Challenge publicChallenge = challengeRepository.save(
                Challenge.createPublicChallenge(
                    "공개 챌린지", "설명", PeriodType.DAILY,
                    LocalDate.now(), LocalDate.now().plusDays(7),
                    testMember.getId(), null
                )
            );
            
            Challenge inviteChallenge = challengeRepository.save(
                Challenge.createInviteOnlyChallenge(
                    "초대 챌린지", "설명", PeriodType.DAILY,
                    LocalDate.now(), LocalDate.now().plusDays(7),
                    testMember.getId(), null
                )
            );

            // when & then
            mockMvc.perform(get("/challenges"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[*].id", hasItem(publicChallenge.getId().intValue())))
                    .andExpect(jsonPath("$.content[*].id", not(hasItem(inviteChallenge.getId().intValue()))));
        }

        @Test
        @DisplayName("키워드 검색")
        void searchChallenges_ByKeyword() throws Exception {
            // given
            challengeRepository.save(
                Challenge.createPublicChallenge(
                    "요가 챌린지", "매일 요가하기", PeriodType.DAILY,
                    LocalDate.now(), LocalDate.now().plusDays(30),
                    testMember.getId(), null
                )
            );
            
            challengeRepository.save(
                Challenge.createPublicChallenge(
                    "독서 챌린지", "매일 책 읽기", PeriodType.DAILY,
                    LocalDate.now(), LocalDate.now().plusDays(30),
                    testMember.getId(), null
                )
            );

            // when & then
            mockMvc.perform(get("/challenges")
                    .param("search", "요가"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content", hasSize(1)))
                    .andExpect(jsonPath("$.content[0].title", containsString("요가")));
        }

        @Test
        @DisplayName("정렬 옵션 테스트")
        void getChallenges_Sorting() throws Exception {
            // given
            for (int i = 0; i < 3; i++) {
                challengeRepository.save(
                    Challenge.createPublicChallenge(
                        "챌린지 " + i, "설명", PeriodType.DAILY,
                        LocalDate.now().plusDays(i), LocalDate.now().plusMonths(1),
                        testMember.getId(), 10 + i * 10
                    )
                );
            }

            // when & then - 최신순
            mockMvc.perform(get("/challenges")
                    .param("sort", "latest"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].title").value("챌린지 2"));

            // when & then - ID 내림차순
            mockMvc.perform(get("/challenges")
                    .param("sort", "id,desc"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].title").value("챌린지 2"));
        }
    }

    @Nested
    @DisplayName("챌린지 참여 테스트")
    class JoinChallengeTest {

        @Test
        @DisplayName("공개 챌린지 정상 참여")
        @WithUserDetails("anon@zzic.com")
        void joinPublicChallenge_Success() throws Exception {
            // given
            Challenge challenge = challengeRepository.save(
                Challenge.createPublicChallenge(
                    "테스트 챌린지", "설명", PeriodType.DAILY,
                    LocalDate.now(), LocalDate.now().plusDays(7),
                    otherMember.getId(), null
                )
            );

            // when & then
            mockMvc.perform(post("/challenges/{challengeId}/participants", challenge.getId()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.nickname").value(testMember.getNickname()));
        }

        @Test
        @DisplayName("초대 코드로 초대 전용 챌린지 참여")
        @WithUserDetails("anon@zzic.com")
        void joinByInviteCode_Success() throws Exception {
            // given
            Challenge challenge = challengeRepository.save(
                Challenge.createInviteOnlyChallenge(
                    "초대 전용", "설명", PeriodType.WEEKLY,
                    LocalDate.now(), LocalDate.now().plusWeeks(4),
                    otherMember.getId(), null
                )
            );

            // when & then
            mockMvc.perform(post("/challenges/join-by-invite")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"inviteCode\": \"" + challenge.getInviteCode() + "\"}"))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("잘못된 초대 코드로 참여 시도")
        @WithUserDetails("anon@zzic.com")
        void joinByInviteCode_InvalidCode() throws Exception {
            mockMvc.perform(post("/challenges/join-by-invite")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"inviteCode\": \"INVALID1\"}"))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.errorCode").value("RESOURCE_001"));
        }

        @Test
        @DisplayName("최대 인원 초과 시 참여 시도")
        @WithUserDetails("anon@zzic.com")
        void joinChallenge_MaxParticipantsExceeded() throws Exception {
            // given
            Challenge challenge = challengeRepository.save(
                Challenge.createPublicChallenge(
                    "인원 제한", "설명", PeriodType.DAILY,
                    LocalDate.now(), LocalDate.now().plusDays(7),
                    otherMember.getId(), 1  // 최대 1명
                )
            );
            
            // 다른 사용자가 먼저 참여
            mockMvc.perform(post("/challenges/{challengeId}/participants", challenge.getId())
                    .with(user("other@example.com")))
                    .andExpect(status().isCreated());

            // when & then - 두 번째 참여 시도
            mockMvc.perform(post("/challenges/{challengeId}/participants", challenge.getId()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errorCode").value("BIZ_001"));
        }

        @Test
        @DisplayName("종료된 챌린지 참여 시도")
        @WithUserDetails("anon@zzic.com")
        void joinChallenge_AlreadyEnded() throws Exception {
            // given
            Challenge challenge = challengeRepository.save(
                Challenge.createPublicChallenge(
                    "종료된 챌린지", "설명", PeriodType.DAILY,
                    LocalDate.now().minusDays(10), LocalDate.now().minusDays(1),
                    otherMember.getId(), null
                )
            );

            // when & then
            mockMvc.perform(post("/challenges/{challengeId}/participants", challenge.getId()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("종료되었거나 시작되지 않은 챌린지입니다"));
        }
    }

    @Nested
    @DisplayName("초대 링크 테스트")
    class InviteLinkTest {

        @Test
        @DisplayName("생성자의 초대 링크 조회 성공")
        @WithUserDetails("anon@zzic.com")
        void getInviteLink_ByCreator() throws Exception {
            // given
            Challenge challenge = challengeRepository.save(
                Challenge.createInviteOnlyChallenge(
                    "초대 전용", "설명", PeriodType.WEEKLY,
                    LocalDate.now(), LocalDate.now().plusWeeks(4),
                    testMember.getId(), null
                )
            );

            // when & then
            mockMvc.perform(get("/challenges/{challengeId}/invite-link", challenge.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.inviteCode").value(challenge.getInviteCode()))
                    .andExpect(jsonPath("$.inviteUrl").value(containsString(challenge.getInviteCode())));
        }

        @Test
        @DisplayName("생성자가 아닌 사용자의 초대 링크 조회 시도")
        @WithUserDetails("anon@zzic.com")
        void getInviteLink_NotCreator() throws Exception {
            // given
            Challenge challenge = challengeRepository.save(
                Challenge.createInviteOnlyChallenge(
                    "초대 전용", "설명", PeriodType.WEEKLY,
                    LocalDate.now(), LocalDate.now().plusWeeks(4),
                    testMember.getId(), null
                )
            );

            // when & then
            mockMvc.perform(get("/challenges/{challengeId}/invite-link", challenge.getId()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errorCode").value("AUTH_002"));
        }

        @Test
        @DisplayName("공개 챌린지의 초대 링크 조회 시도")
        @WithUserDetails("anon@zzic.com")
        void getInviteLink_PublicChallenge() throws Exception {
            // given
            Challenge challenge = challengeRepository.save(
                Challenge.createPublicChallenge(
                    "공개 챌린지", "설명", PeriodType.DAILY,
                    LocalDate.now(), LocalDate.now().plusDays(7),
                    testMember.getId(), null
                )
            );

            // when & then
            mockMvc.perform(get("/challenges/{challengeId}/invite-link", challenge.getId()))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.detail").value("공개 챌린지는 초대 링크가 필요 없습니다"));
        }
    }

    @Nested
    @DisplayName("챌린지 투두 테스트")
    class ChallengeTodoTest {

        @Test
        @DisplayName("일간 챌린지 투두 완료")
        @WithUserDetails("anon@zzic.com")
        void completeDailyChallengeTodo() throws Exception {
            // given
            Challenge challenge = challengeRepository.save(
                Challenge.createPublicChallenge(
                    "일간 챌린지", "설명", PeriodType.DAILY,
                    LocalDate.now(), LocalDate.now().plusDays(7),
                    otherMember.getId(), null
                )
            );
            
            // 먼저 참여
            mockMvc.perform(post("/challenges/{challengeId}/participants", challenge.getId()))
                    .andExpect(status().isCreated());

            // when & then - 투두 완료
            mockMvc.perform(post("/challenges/{challengeId}/todos", challenge.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.done").value(true))
                    .andExpect(jsonPath("$.challengeTitle").value("일간 챌린지"));
        }

        @Test
        @DisplayName("같은 기간에 중복 완료 시도")
        @WithUserDetails("anon@zzic.com")
        void completeTodo_Duplicate() throws Exception {
            // given
            Challenge challenge = challengeRepository.save(
                Challenge.createPublicChallenge(
                    "일간 챌린지", "설명", PeriodType.DAILY,
                    LocalDate.now(), LocalDate.now().plusDays(7),
                    otherMember.getId(), null
                )
            );
            
            // 참여 및 첫 번째 완료
            mockMvc.perform(post("/challenges/{challengeId}/participants", challenge.getId()))
                    .andExpect(status().isCreated());
            mockMvc.perform(post("/challenges/{challengeId}/todos", challenge.getId()))
                    .andExpect(status().isOk());

            // when & then - 두 번째 완료 시도
            mockMvc.perform(post("/challenges/{challengeId}/todos", challenge.getId()))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.errorCode").value("RESOURCE_002"));
        }

        @Test
        @DisplayName("참여하지 않은 챌린지의 투두 완료 시도")
        @WithUserDetails("anon@zzic.com")
        void completeTodo_NotParticipating() throws Exception {
            // given
            Challenge challenge = challengeRepository.save(
                Challenge.createPublicChallenge(
                    "테스트 챌린지", "설명", PeriodType.DAILY,
                    LocalDate.now(), LocalDate.now().plusDays(7),
                    otherMember.getId(), null
                )
            );

            // when & then
            mockMvc.perform(post("/challenges/{challengeId}/todos", challenge.getId()))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.errorCode").value("AUTH_002"));
        }
    }

    @Nested
    @DisplayName("옵션 조회 테스트")
    class OptionsTest {

        @Test
        @DisplayName("가시성 옵션 조회")
        void getVisibilityOptions() throws Exception {
            mockMvc.perform(get("/challenges/visibility-options"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$", hasSize(2)))
                    .andExpect(jsonPath("$[*].code", containsInAnyOrder("PUBLIC", "INVITE_ONLY")))
                    .andExpect(jsonPath("$[*].description", containsInAnyOrder("공개", "초대 전용")));
        }

        @Test
        @DisplayName("정책 옵션 조회")
        void getPolicyOptions() throws Exception {
            mockMvc.perform(get("/challenges/policy-options"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").exists())
                    .andExpect(jsonPath("$[0].code").exists())
                    .andExpect(jsonPath("$[0].name").exists())
                    .andExpect(jsonPath("$[0].description").exists());
        }
    }

    @Nested
    @DisplayName("동시성 테스트")
    class ConcurrencyTest {

        @Test
        @DisplayName("동시 다발적 참여 요청 시 최대 인원 제한 준수")
        void concurrentJoin_RespectMaxParticipants() throws Exception {
            // given
            Challenge challenge = challengeRepository.save(
                Challenge.createPublicChallenge(
                    "인원 제한", "설명", PeriodType.DAILY,
                    LocalDate.now(), LocalDate.now().plusDays(7),
                    testMember.getId(), 5  // 최대 5명
                )
            );

            // when - 10개의 동시 참여 요청
            List<Thread> threads = new ArrayList<>();
            List<Integer> successCount = Collections.synchronizedList(new ArrayList<>());
            
            for (int i = 0; i < 10; i++) {
                final int userId = i;
                Thread thread = new Thread(() -> {
                    try {
                        CreateMemberCommand userCommand = new CreateMemberCommand(
                            "user" + userId + "@example.com",
                            "password",
                            "유저" + userId,
                            null
                        );
                        Member member = memberService.createMember(userCommand);
                        
                        mockMvc.perform(post("/challenges/{challengeId}/participants", challenge.getId())
                                .with(user(member.getEmail())))
                                .andExpect(status().isCreated());
                        
                        successCount.add(1);
                    } catch (Exception e) {
                        // 실패는 정상
                    }
                });
                threads.add(thread);
                thread.start();
            }
            
            // 모든 스레드 종료 대기
            for (Thread thread : threads) {
                thread.join();
            }
            
            // then - 최대 5명만 성공
            assert successCount.size() <= 5;
        }
    }
}