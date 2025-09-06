package point.ttodoApi.shared.config.member;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.*;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import point.ttodoApi.member.application.MemberService;
import point.ttodoApi.member.domain.Member;
import point.ttodoApi.member.infrastructure.persistence.MemberRepository;
import point.ttodoApi.profile.application.ProfileService;
import point.ttodoApi.profile.domain.*;
import point.ttodoApi.profile.infrastructure.persistence.ProfileRepository;
import point.ttodoApi.shared.config.properties.AppProperties;

import java.util.UUID;

import static point.ttodoApi.shared.constants.SystemConstants.SystemUsers.*;

/**
 * 멤버 및 프로필 초기화 담당
 * 시스템 사용자(익명, 루트) 및 시드 멤버 생성
 * 실행 순서: 2번 (Level 초기화 이후)
 */
@Slf4j
@Component
@Order(2)
@RequiredArgsConstructor
public class MemberInitializer implements ApplicationRunner {
  private final AppProperties appProperties;

  private final MemberRepository memberRepository;
  private final ProfileRepository profileRepository;
  private final PasswordEncoder passwordEncoder;
  private final MemberService memberService;
  private final ProfileService profileService;

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    if (isAlreadyInitialized()) {
      log.info("Member initialization already completed, skipping");
      return;
    }

    try {
      initializeSystemUsers();
      initializeSeedMembers();
      log.info("Member initialization completed successfully");
    } catch (Exception e) {
      log.error("Member initialization failed", e);
      throw e;
    }
  }

  /**
   * 시스템 사용자 초기화 여부 확인
   *
   * @return 이미 초기화된 경우 true
   */
  private boolean isAlreadyInitialized() {
    return memberRepository.existsById(ANON_USER_ID);
  }

  /**
   * 시스템 사용자 초기화
   */
  public void initializeSystemUsers() {
    log.info("Initializing system users...");

    // 익명 사용자 생성
    String domain = appProperties.getUserDomain();
    Member anonMember = createSystemUser(
            ANON_USER_ID,
            "anon@" + domain,
            ANON_USER_PASSWORD,
            ANON_USER_NICKNAME,
            "시스템 익명 사용자"
    );

    // 루트 사용자 생성
    Member rootMember = createSystemUser(
            ROOT_USER_ID,
            "root@" + domain,
            ROOT_USER_PASSWORD,
            ROOT_USER_NICKNAME,
            "시스템 루트 관리자"
    );

    log.info("System users created: {} and {}", "anon@" + domain, "root@" + domain);
  }

  /**
   * 시드 멤버 초기화: 익명 사용자만 유지
   */
  public Member[] initializeSeedMembers() {
    log.info("Initializing seed members (anon only)...");
    Member anonMember = memberRepository.findById(ANON_USER_ID).orElseThrow();
    updateAnonUserProfile(anonMember);
    log.info("Initialized 1 seed member (anon)");
    return new Member[]{anonMember};
  }

  /**
   * 시스템 사용자 생성
   */
  private Member createSystemUser(UUID id, String email, String password, String nickname, String introduction) {
    Member member = Member.builder()
            .id(id)
            .email(email)
            .password(passwordEncoder.encode(password))
            .nickname(nickname)
            .build();
    memberRepository.save(member);

    Profile profile = Profile.builder()
            .ownerId(id)
            .introduction(introduction)
            .theme(Theme.PINKY)
            .timeZone("Asia/Seoul")
            .locale("ko-KR")
            .build();
    profileRepository.save(profile);

    return member;
  }

  /**
   * 익명 사용자 프로필 업데이트
   */
  private void updateAnonUserProfile(Member member) {
    try {
      var profile = profileService.getProfile(member.getId());
      profile.updateIntroduction("안녕하세요! 저는 전설의 홍길동입니다. 🐭 매일 꾸준히 할 일을 완료하며 성장하고 있어요!");
      profileService.saveProfile(profile);
    } catch (Exception e) {
      log.debug("Failed to update profile introduction: {}", e.getMessage());
    }
  }
}
