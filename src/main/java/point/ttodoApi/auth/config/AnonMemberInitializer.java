package point.ttodoApi.auth.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import point.ttodoApi.challenge.application.*;
import point.ttodoApi.challenge.application.dto.command.CreateChallengeCommand;
import point.ttodoApi.challenge.domain.*;
import point.ttodoApi.member.application.MemberService;
import point.ttodoApi.member.application.dto.command.CreateMemberCommand;
import point.ttodoApi.member.domain.Member;
import point.ttodoApi.profile.application.ProfileService;
import point.ttodoApi.todo.config.TodoInitializer;

import java.time.LocalDate;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnonMemberInitializer implements ApplicationRunner {
  private final MemberService memberService;
  private final PasswordEncoder passwordEncoder;
  private final ChallengeService challengeService;
  private final ChallengeParticipationService participationService;
  private final ChallengeTodoService challengeTodoService;
  private final TodoInitializer todoInitializer;
  private final ProfileService profileService;
  private final java.util.Random random = new java.util.Random();

  @Override
  public void run(ApplicationArguments args) {
    log.info("Starting seed data initialization...");
    Member[] members = createSeedMembers();
    Long[] challengeIds = createSeedChallenges();
    seedParticipations(members, challengeIds);
    log.info("Seed data initialization completed!");
  }

  private Member[] createSeedMembers() {
    Member[] members = new Member[11];

    Member member = createOrFindMember("anon@ttodo.dev", "", "전설의찍찍이");
    members[0] = member;
    
    // 첫 번째 익명 사용자의 프로필에 자기소개 추가
    try {
      var profile = profileService.getProfile(member.getId());
      profile.updateIntroduction("안녕하세요! 저는 전설의찍찍이입니다. 🐭 매일 꾸준히 할 일을 완료하며 성장하고 있어요!");
      profileService.saveProfile(profile);
    } catch (Exception e) {
      log.debug("Failed to update profile introduction: {}", e.getMessage());
    }
    
    // 첫 번째 익명 사용자에게만 기본 할일 생성
    todoInitializer.createDefaultTodosForMember(member);
    
    for (int i = 1; i < 11; i++) {
      String email = "anon" + (i + 1) + "@ttodo.com";
      String nickname = switch (i) {
        case 0 -> "일일챌린저";
        case 1 -> "월간도전자";
        case 2 -> "전략적참여자";
        case 3 -> "운동매니아";
        case 4 -> "독서광";
        case 5 -> "습관왕";
        case 6 -> "아침형인간";
        case 7 -> "야행성참가자";
        case 8 -> "도전러";
        default -> "챌린지러" + (i + 1);
      };
      members[i] = createOrFindMember(email, "", nickname);
    }
    return members;
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

  private Member createOrFindMember(String email, String password, String nickname) {
    if (memberService.findByEmail(email).isEmpty()) {
      CreateMemberCommand command = new CreateMemberCommand(
          email,
          passwordEncoder.encode(password),
          nickname,
          null
      );
      Member member = memberService.createMember(command);
      log.info("Created member: {}", nickname);
      return member;
    } else {
      Member member = memberService.findByEmailOrThrow(email);
      log.info("Found existing member: {}", nickname);
      return member;
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
