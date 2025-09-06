package point.ttodoApi.shared.config.challenge;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.*;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import point.ttodoApi.challenge.application.*;
import point.ttodoApi.challenge.application.command.CreateChallengeCommand;
import point.ttodoApi.challenge.domain.*;
import point.ttodoApi.challenge.infrastructure.ChallengeRepository;
import point.ttodoApi.member.domain.Member;
import point.ttodoApi.member.infrastructure.persistence.MemberRepository;

import java.time.LocalDate;
import java.util.*;

/**
 * 챌린지 초기화 담당
 * 기본 챌린지 생성 및 참여 데이터 초기화
 * 실행 순서: 4번 (Member, Todo 초기화 이후)
 */
@Slf4j
@Component
@Order(4)
@RequiredArgsConstructor
public class ChallengeInitializer implements ApplicationRunner {

  private final ChallengeService challengeService;
  private final ChallengeParticipationService participationService;
  private final ChallengeTodoService challengeTodoService;
  private final ChallengeRepository challengeRepository;
  private final MemberRepository memberRepository;

  private final Random random = new Random();

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    if (isAlreadyInitialized()) {
      log.info("Challenge initialization already completed, skipping");
      return;
    }

    try {
      Long[] challengeIds = initializeChallenges();
      Member[] members = getMembersForParticipation();
      initializeChallengeParticipations(members, challengeIds);
      log.info("Challenge initialization completed successfully");
    } catch (Exception e) {
      log.error("Challenge initialization failed", e);
      throw e;
    }
  }

  /**
   * 챌린지 초기화 여부 확인
   */
  private boolean isAlreadyInitialized() {
    return challengeRepository.count() > 0;
  }

  /**
   * 참여할 멤버 목록 조회
   */
  private Member[] getMembersForParticipation() {
    List<Member> members = memberRepository.findAll();
    return members.toArray(new Member[0]);
  }

  /**
   * 챌린지 초기화
   *
   * @return 생성된 챌린지 ID 배열
   */
  public Long[] initializeChallenges() {
    log.info("Initializing challenges...");

    ChallengeSeed[] challengeSeeds = createChallengeSeeds();
    Long[] challengeIds = new Long[challengeSeeds.length];
    LocalDate now = LocalDate.now();

    for (int i = 0; i < challengeSeeds.length; i++) {
      var seed = challengeSeeds[i];
      CreateChallengeCommand command = new CreateChallengeCommand(
              seed.title(),
              seed.desc(),
              seed.type(),
              ChallengeVisibility.PUBLIC,
              now,
              now.plusMonths(6),
              null, // maxParticipants
              UUID.randomUUID(), // creatorId
              null  // policyIds
      );
      challengeIds[i] = challengeService.createChallenge(command);
      log.debug("Created challenge: {}", seed.title());
    }

    log.info("Initialized {} challenges", challengeIds.length);
    return challengeIds;
  }

  /**
   * 챌린지 참여 초기화
   *
   * @param members      참여할 멤버 배열
   * @param challengeIds 챌린지 ID 배열
   */
  public void initializeChallengeParticipations(Member[] members, Long[] challengeIds) {
    log.info("Initializing challenge participations...");

    int totalParticipations = 0;

    for (Member member : members) {
      int participationCount = 3 + random.nextInt(4); // 3~6개
      Set<Integer> joined = new HashSet<>();

      while (joined.size() < participationCount) {
        int idx = random.nextInt(challengeIds.length);
        if (joined.add(idx)) {
          try {
            participationService.joinChallenge(challengeIds[idx], member);
            totalParticipations++;

            // 50% 확률로 완료 처리
            if (random.nextBoolean()) {
              challengeTodoService.completeChallenge(challengeIds[idx], member, LocalDate.now());
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
   * 기본 챌린지 데이터
   */
  private record ChallengeSeed(String title, String desc, PeriodType type) {
  }
}