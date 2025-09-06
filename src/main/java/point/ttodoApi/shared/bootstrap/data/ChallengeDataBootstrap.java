package point.ttodoApi.shared.bootstrap.data;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import point.ttodoApi.challenge.application.ChallengeParticipationService;
import point.ttodoApi.challenge.application.ChallengeService;
import point.ttodoApi.challenge.application.ChallengeTodoService;
import point.ttodoApi.challenge.application.command.CreateChallengeCommand;
import point.ttodoApi.challenge.domain.ChallengeVisibility;
import point.ttodoApi.challenge.domain.PeriodType;
import point.ttodoApi.challenge.infrastructure.ChallengeRepository;
import point.ttodoApi.member.domain.Member;
import point.ttodoApi.member.infrastructure.persistence.MemberRepository;

import java.time.LocalDate;
import java.util.*;

/**
 * 챌린지 초기 데이터 생성
 * 기본 챌린지 및 참여 데이터 생성
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChallengeDataBootstrap {

    private final ChallengeService challengeService;
    private final ChallengeParticipationService participationService;
    private final ChallengeTodoService challengeTodoService;
    private final ChallengeRepository challengeRepository;
    private final MemberRepository memberRepository;

    private final Random random = new Random();

    public void initialize() {
        if (challengeRepository.count() > 0) {
            log.info("Challenges already exist, skipping challenge initialization");
            return;
        }

        try {
            Long[] challengeIds = createBasicChallenges();
            Member[] members = getMembersForParticipation();
            
            if (members.length > 0) {
                initializeChallengeParticipations(members, challengeIds);
            }
            
            log.info("Challenge initialization completed successfully");
        } catch (Exception e) {
            log.error("Challenge initialization failed", e);
            throw e;
        }
    }

    /**
     * 기본 챌린지 생성
     */
    private Long[] createBasicChallenges() {
        log.info("Creating basic challenges...");

        ChallengeSeed[] challengeSeeds = createChallengeSeeds();
        Long[] challengeIds = new Long[challengeSeeds.length];
        LocalDate now = LocalDate.now();

        for (int i = 0; i < challengeSeeds.length; i++) {
            var seed = challengeSeeds[i];
            CreateChallengeCommand command = new CreateChallengeCommand(
                    seed.title(),
                    seed.description(),
                    seed.periodType(),
                    ChallengeVisibility.PUBLIC,
                    now,
                    now.plusMonths(6),
                    null, // maxParticipants
                    UUID.randomUUID(), // creatorId (임시)
                    null  // policyIds
            );
            challengeIds[i] = challengeService.createChallenge(command);
            log.debug("Created challenge: {}", seed.title());
        }

        log.info("Created {} basic challenges", challengeIds.length);
        return challengeIds;
    }

    /**
     * 참여할 멤버 목록 조회
     */
    private Member[] getMembersForParticipation() {
        List<Member> members = memberRepository.findAll();
        return members.toArray(new Member[0]);
    }

    /**
     * 챌린지 참여 초기화
     */
    private void initializeChallengeParticipations(Member[] members, Long[] challengeIds) {
        log.info("Initializing challenge participations...");

        int totalParticipations = 0;

        for (Member member : members) {
            int participationCount = 3 + random.nextInt(4); // 3~6개 참여
            Set<Integer> joinedChallenges = new HashSet<>();

            while (joinedChallenges.size() < participationCount && joinedChallenges.size() < challengeIds.length) {
                int challengeIndex = random.nextInt(challengeIds.length);
                if (joinedChallenges.add(challengeIndex)) {
                    try {
                        participationService.joinChallenge(challengeIds[challengeIndex], member);
                        totalParticipations++;

                        // 50% 확률로 완료 처리
                        if (random.nextBoolean()) {
                            challengeTodoService.completeChallenge(
                                challengeIds[challengeIndex], 
                                member, 
                                LocalDate.now()
                            );
                        }
                    } catch (Exception e) {
                        log.debug("Challenge participation failed: {}", e.getMessage());
                    }
                }
            }
        }

        log.info("Initialized {} challenge participations", totalParticipations);
    }

    /**
     * 기본 챌린지 시드 데이터 생성
     */
    private ChallengeSeed[] createChallengeSeeds() {
        return new ChallengeSeed[]{
                new ChallengeSeed("일일 물 마시기", "하루에 물 8잔 이상 마시기", PeriodType.DAILY),
                new ChallengeSeed("주간 독서하기", "일주일에 책 1권 읽기", PeriodType.WEEKLY),
                new ChallengeSeed("월간 운동하기", "한 달 동안 운동 20회 이상 하기", PeriodType.MONTHLY),
                new ChallengeSeed("아침 6시 기상", "매일 아침 6시에 일어나기", PeriodType.DAILY),
                new ChallengeSeed("야식 끊기", "야식 먹지 않기 도전", PeriodType.DAILY),
                new ChallengeSeed("주간 러닝", "일주일에 3회 이상 달리기", PeriodType.WEEKLY),
                new ChallengeSeed("월간 영어공부", "한 달 동안 영어공부 15회 이상", PeriodType.MONTHLY),
                new ChallengeSeed("일일 명상", "매일 10분 명상하기", PeriodType.DAILY),
                new ChallengeSeed("주간 영화감상", "일주일에 영화 2편 보기", PeriodType.WEEKLY),
                new ChallengeSeed("월간 저축", "한 달 동안 10만원 이상 저축하기", PeriodType.MONTHLY)
        };
    }

    /**
     * 챌린지 시드 데이터 레코드
     */
    private record ChallengeSeed(String title, String description, PeriodType periodType) {
    }
}