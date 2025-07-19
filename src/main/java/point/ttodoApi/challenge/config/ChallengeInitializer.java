package point.ttodoApi.challenge.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import point.ttodoApi.challenge.application.ChallengeParticipationService;
import point.ttodoApi.challenge.application.ChallengeService;
import point.ttodoApi.challenge.application.ChallengeTodoService;
import point.ttodoApi.challenge.application.dto.command.CreateChallengeCommand;
import point.ttodoApi.challenge.domain.ChallengeVisibility;
import point.ttodoApi.challenge.domain.PeriodType;
import point.ttodoApi.member.config.MemberInitializer;
import point.ttodoApi.member.domain.Member;

import java.time.LocalDate;
import java.util.UUID;

/**
 * 챌린지 관련 데이터 초기화
 */
@Slf4j
@Component
@Order(3)
@RequiredArgsConstructor
@org.springframework.context.annotation.Profile("!test")
public class ChallengeInitializer implements ApplicationRunner {
    
    private final ChallengeService challengeService;
    private final ChallengeParticipationService participationService;
    private final ChallengeTodoService challengeTodoService;
    private final MemberInitializer memberInitializer;
    private final java.util.Random random = new java.util.Random();
    
    @Override
    public void run(ApplicationArguments args) {
        log.info("Starting challenge initialization...");
        Member[] members = memberInitializer.getCreatedMembers();
        Long[] challengeIds = createSeedChallenges();
        seedParticipations(members, challengeIds);
        log.info("Challenge initialization completed!");
    }
    
    private Long[] createSeedChallenges() {
        record ChallengeSeed(String title, String desc, PeriodType type) {}
        ChallengeSeed[] challengeSeeds = new ChallengeSeed[] {
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
        Long[] challengeIds = new Long[challengeSeeds.length];
        for (int i = 0; i < challengeSeeds.length; i++) {
            var c = challengeSeeds[i];
            challengeIds[i] = createChallengeIfNotExists(c.title(), c.desc(), c.type());
        }
        return challengeIds;
    }
    
    private void seedParticipations(Member[] members, Long[] challengeIds) {
        for (Member member : members) {
            int participationCount = 3 + random.nextInt(4); // 3~6개
            java.util.Set<Integer> joined = new java.util.HashSet<>();
            while (joined.size() < participationCount) {
                int idx = random.nextInt(challengeIds.length);
                if (joined.add(idx)) {
                    joinChallenge(challengeIds[idx], member);
                    // 50% 확률로 완료 처리
                    if (random.nextBoolean()) {
                        try {
                            challengeTodoService.completeChallenge(challengeIds[idx], member, java.time.LocalDate.now());
                        } catch (Exception e) {
                            log.debug("완료 처리 실패: {}", e.getMessage());
                        }
                    }
                }
            }
        }
    }
    
    private Long createChallengeIfNotExists(String title, String description, PeriodType periodType) {
        try {
            LocalDate now = LocalDate.now();
            CreateChallengeCommand command = new CreateChallengeCommand(
                title, 
                description, 
                periodType,
                ChallengeVisibility.PUBLIC,
                now,
                now.plusMonths(6),
                null, // maxParticipants
                UUID.randomUUID(), // creatorId
                null  // policyIds
            );
            Long challengeId = challengeService.createChallenge(command);
            log.info("Created challenge: {}", title);
            return challengeId;
        } catch (Exception e) {
            log.debug("Challenge creation failed: {}", e.getMessage());
            return null;
        }
    }
    
    private void joinChallenge(Long challengeId, Member member) {
        try {
            participationService.joinChallenge(challengeId, member);
            log.debug("Member {} joined challenge {}", member.getNickname(), challengeId);
        } catch (Exception e) {
            log.debug("Challenge join skipped for member {} and challenge {}: {}", 
                member.getNickname(), challengeId, e.getMessage());
        }
    }
}