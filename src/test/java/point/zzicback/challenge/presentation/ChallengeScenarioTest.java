package point.zzicback.challenge.presentation;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;
import point.zzicback.challenge.domain.Challenge;
import point.zzicback.challenge.domain.ChallengeParticipation;
import point.zzicback.challenge.domain.PeriodType;
import point.zzicback.challenge.infrastructure.ChallengeParticipationRepository;
import point.zzicback.challenge.infrastructure.ChallengeRepository;
import point.zzicback.challenge.infrastructure.ChallengeTodoRepository;
import point.zzicback.member.domain.Member;
import point.zzicback.member.infrastructure.persistence.MemberRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("챌린지 통합 시나리오 테스트")
class ChallengeScenarioTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ChallengeRepository challengeRepository;

    @Autowired
    private ChallengeParticipationRepository participationRepository;

    @Autowired
    private ChallengeTodoRepository todoRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member creator;
    private Member participant1;
    private Member participant2;

    @BeforeEach
    void setUp() {
        creator = memberRepository.save(Member.builder()
                .email("creator@example.com")
                .nickname("챌린지생성자")
                .build());
        
        participant1 = memberRepository.save(Member.builder()
                .email("participant1@example.com")
                .nickname("참여자1")
                .build());
        
        participant2 = memberRepository.save(Member.builder()
                .email("participant2@example.com")
                .nickname("참여자2")
                .build());
    }

    @Test
    @DisplayName("전체 시나리오: 공개 챌린지 생성 → 참여 → 투두 완료 → 통계 확인")
    void publicChallengeFullScenario() throws Exception {
        // 1. 챌린지 생성
        ResultActions createResult = mockMvc.perform(post("/challenges")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", "30일 운동 챌린지")
                .param("description", "매일 30분 이상 운동하기")
                .param("periodType", "DAILY")
                .param("visibility", "PUBLIC")
                .param("startDate", LocalDate.now().toString())
                .param("endDate", LocalDate.now().plusDays(30).toString())
                .param("maxParticipants", "20")
                .with(user(creator.getEmail())))
                .andExpect(status().isCreated());
        
        Long challengeId = extractChallengeId(createResult);
        
        // 2. 챌린지 목록에서 확인
        mockMvc.perform(get("/challenges"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == " + challengeId + ")].title")
                        .value(hasItem("30일 운동 챌린지")));
        
        // 3. 참여자들 참여
        mockMvc.perform(post("/challenges/{challengeId}/participants", challengeId)
                .with(user(participant1.getEmail())))
                .andExpect(status().isCreated());
        
        mockMvc.perform(post("/challenges/{challengeId}/participants", challengeId)
                .with(user(participant2.getEmail())))
                .andExpect(status().isCreated());
        
        // 4. 참여자 목록 확인
        mockMvc.perform(get("/challenges/{challengeId}/participants", challengeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].nickname", 
                        containsInAnyOrder("참여자1", "참여자2")));
        
        // 5. 투두 완료
        mockMvc.perform(post("/challenges/{challengeId}/todos", challengeId)
                .with(user(participant1.getEmail())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.done").value(true));
        
        // 6. 챌린지 상세 조회 (성공률 포함)
        mockMvc.perform(get("/challenges/{challengeId}", challengeId)
                .with(user(participant1.getEmail())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participated").value(true))
                .andExpect(jsonPath("$.participantCount").value(2))
                .andExpect(jsonPath("$.successRate").value(50.0)); // 2명 중 1명 완료
        
        // 7. 투두 목록 조회
        mockMvc.perform(get("/challenges/todos")
                .with(user(participant1.getEmail())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].challengeTitle").value("30일 운동 챌린지"))
                .andExpect(jsonPath("$.content[0].done").value(true));
    }

    @Test
    @DisplayName("전체 시나리오: 초대 링크 챌린지 생성 → 공유 → 참여")
    void inviteOnlyChallengeScenario() throws Exception {
        // 1. 초대 전용 챌린지 생성
        ResultActions createResult = mockMvc.perform(post("/challenges")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", "비밀 독서 모임")
                .param("description", "매주 한 권씩 읽고 토론")
                .param("periodType", "WEEKLY")
                .param("visibility", "INVITE_ONLY")
                .param("startDate", LocalDate.now().toString())
                .param("endDate", LocalDate.now().plusWeeks(12).toString())
                .param("maxParticipants", "10")
                .with(user(creator.getEmail())))
                .andExpect(status().isCreated());
        
        Long challengeId = extractChallengeId(createResult);
        
        // 2. 공개 목록에서 보이지 않음 확인
        mockMvc.perform(get("/challenges"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.id == " + challengeId + ")]").doesNotExist());
        
        // 3. 초대 링크 조회
        ResultActions inviteLinkResult = mockMvc.perform(
                get("/challenges/{challengeId}/invite-link", challengeId)
                .with(user(creator.getEmail())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inviteCode").exists())
                .andExpect(jsonPath("$.inviteUrl").exists());
        
        String inviteCode = extractInviteCode(inviteLinkResult);
        
        // 4. 직접 참여 시도 (실패해야 함)
        mockMvc.perform(post("/challenges/{challengeId}/participants", challengeId)
                .with(user(participant1.getEmail())))
                .andExpect(status().isForbidden());
        
        // 5. 초대 코드로 참여 (성공)
        mockMvc.perform(post("/challenges/join-by-invite")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"inviteCode\": \"" + inviteCode + "\"}")
                .with(user(participant1.getEmail())))
                .andExpect(status().isCreated());
        
        // 6. 참여 확인
        mockMvc.perform(get("/challenges/{challengeId}", challengeId)
                .with(user(participant1.getEmail())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participated").value(true));
    }

    @Test
    @DisplayName("정책 시나리오: 비활동 자동 퇴장")
    void policyViolationScenario() throws Exception {
        // 1. 정책이 포함된 챌린지 생성
        ResultActions createResult = mockMvc.perform(post("/challenges")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", "엄격한 챌린지")
                .param("description", "7일 이상 미완료시 자동 퇴장")
                .param("periodType", "DAILY")
                .param("visibility", "PUBLIC")
                .param("startDate", LocalDate.now().minusDays(10).toString())
                .param("endDate", LocalDate.now().plusDays(20).toString())
                .param("policyIds", "1") // 비활동 정책 ID
                .with(user(creator.getEmail())))
                .andExpect(status().isCreated());
        
        Long challengeId = extractChallengeId(createResult);
        
        // 2. 참여자 참여
        mockMvc.perform(post("/challenges/{challengeId}/participants", challengeId)
                .with(user(participant1.getEmail())))
                .andExpect(status().isCreated());
        
        // 3. 참여 상태를 8일 전으로 조작 (테스트 목적)
        ChallengeParticipation participation = participationRepository
                .findByChallengeIdAndMemberId(challengeId, participant1.getId())
                .orElseThrow();
        participation.setJoinedAt(LocalDateTime.now().minusDays(8));
        participationRepository.save(participation);
        
        // 4. 정책 실행 시뮬레이션 (실제로는 스케줄러가 실행)
        // 여기서는 서비스 메서드를 직접 호출하거나 이벤트를 발생시켜야 함
        
        // 5. 퇴장 확인
        mockMvc.perform(get("/challenges/{challengeId}", challengeId)
                .with(user(participant1.getEmail())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participated").value(false));
    }

    @Test
    @DisplayName("챌린지 수정 시나리오")
    void updateChallengeScenario() throws Exception {
        // 1. 챌린지 생성
        ResultActions createResult = mockMvc.perform(post("/challenges")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", "원래 제목")
                .param("description", "원래 설명")
                .param("periodType", "DAILY")
                .param("visibility", "PUBLIC")
                .param("startDate", LocalDate.now().plusDays(5).toString())
                .param("endDate", LocalDate.now().plusDays(35).toString())
                .param("maxParticipants", "10")
                .with(user(creator.getEmail())))
                .andExpect(status().isCreated());
        
        Long challengeId = extractChallengeId(createResult);
        
        // 2. 시작 전 수정 (성공)
        mockMvc.perform(patch("/challenges/{challengeId}", challengeId)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", "수정된 제목")
                .param("description", "수정된 설명")
                .param("maxParticipants", "20")
                .with(user(creator.getEmail())))
                .andExpect(status().isNoContent());
        
        // 3. 수정 확인
        mockMvc.perform(get("/challenges/{challengeId}", challengeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("수정된 제목"))
                .andExpect(jsonPath("$.description").value("수정된 설명"));
        
        // 4. 다른 사용자의 수정 시도 (실패)
        mockMvc.perform(patch("/challenges/{challengeId}", challengeId)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("title", "해커의 제목")
                .with(user(participant1.getEmail())))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("탈퇴 및 재참여 시나리오")
    void leaveAndRejoinScenario() throws Exception {
        // 1. 챌린지 생성
        Challenge challenge = challengeRepository.save(
            Challenge.createPublicChallenge(
                "재참여 가능", "탈퇴 후 다시 참여 가능", PeriodType.WEEKLY,
                LocalDate.now(), LocalDate.now().plusWeeks(4),
                creator.getId(), null
            )
        );
        
        // 2. 참여
        mockMvc.perform(post("/challenges/{challengeId}/participants", challenge.getId())
                .with(user(participant1.getEmail())))
                .andExpect(status().isCreated());
        
        // 3. 투두 완료
        mockMvc.perform(post("/challenges/{challengeId}/todos", challenge.getId())
                .with(user(participant1.getEmail())))
                .andExpect(status().isOk());
        
        // 4. 탈퇴
        mockMvc.perform(delete("/challenges/{challengeId}/participants", challenge.getId())
                .with(user(participant1.getEmail())))
                .andExpect(status().isNoContent());
        
        // 5. 탈퇴 확인
        mockMvc.perform(get("/challenges/{challengeId}", challenge.getId())
                .with(user(participant1.getEmail())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.participated").value(false));
        
        // 6. 재참여
        mockMvc.perform(post("/challenges/{challengeId}/participants", challenge.getId())
                .with(user(participant1.getEmail())))
                .andExpect(status().isCreated());
        
        // 7. 이전 투두 기록은 유지되는지 확인
        long todoCount = todoRepository.countByChallengeParticipation_Challenge_IdAndChallengeParticipation_Member_Id(
                challenge.getId(), participant1.getId());
        assertEquals(1, todoCount, "이전 투두 기록이 유지되어야 함");
    }

    @Test
    @DisplayName("동시성 시나리오: 마지막 자리 경쟁")
    void concurrencyLastSpotScenario() throws Exception {
        // 1. 최대 3명 챌린지 생성
        Challenge challenge = challengeRepository.save(
            Challenge.createPublicChallenge(
                "한정판", "선착순 3명", PeriodType.DAILY,
                LocalDate.now(), LocalDate.now().plusDays(7),
                creator.getId(), 3
            )
        );
        
        // 2. 2명 먼저 참여
        mockMvc.perform(post("/challenges/{challengeId}/participants", challenge.getId())
                .with(user(creator.getEmail())))
                .andExpect(status().isCreated());
        
        mockMvc.perform(post("/challenges/{challengeId}/participants", challenge.getId())
                .with(user(participant1.getEmail())))
                .andExpect(status().isCreated());
        
        // 3. 마지막 자리를 놓고 2명이 동시 요청
        List<Thread> threads = new ArrayList<>();
        List<Integer> results = Collections.synchronizedList(new ArrayList<>());
        
        Thread thread1 = new Thread(() -> {
            try {
                mockMvc.perform(post("/challenges/{challengeId}/participants", challenge.getId())
                        .with(user(participant2.getEmail())))
                        .andExpect(status().isCreated());
                results.add(201);
            } catch (Exception e) {
                results.add(400);
            }
        });
        
        Member participant3 = memberRepository.save(Member.builder()
                .email("participant3@example.com")
                .nickname("참여자3")
                .build());
        
        Thread thread2 = new Thread(() -> {
            try {
                mockMvc.perform(post("/challenges/{challengeId}/participants", challenge.getId())
                        .with(user(participant3.getEmail())))
                        .andExpect(status().isCreated());
                results.add(201);
            } catch (Exception e) {
                results.add(400);
            }
        });
        
        threads.add(thread1);
        threads.add(thread2);
        thread1.start();
        thread2.start();
        
        for (Thread thread : threads) {
            thread.join();
        }
        
        // 4. 결과 확인 - 정확히 한 명만 성공
        long successCount = results.stream().filter(r -> r == 201).count();
        assertEquals(1, successCount, "정확히 한 명만 참여에 성공해야 함");
        
        // 5. 최종 참여자 수 확인
        assertEquals(3, challenge.getActiveParticipantCount());
    }

    private Long extractChallengeId(ResultActions result) throws Exception {
        String response = result.andReturn().getResponse().getContentAsString();
        return Long.parseLong(response.replaceAll(".*\"challengeId\":(\\d+).*", "$1"));
    }

    private String extractInviteCode(ResultActions result) throws Exception {
        String response = result.andReturn().getResponse().getContentAsString();
        return response.replaceAll(".*\"inviteCode\":\"([^\"]+)\".*", "$1");
    }
}