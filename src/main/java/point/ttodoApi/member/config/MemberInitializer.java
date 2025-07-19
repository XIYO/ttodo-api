package point.ttodoApi.member.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import point.ttodoApi.member.application.MemberService;
import point.ttodoApi.member.application.dto.command.CreateMemberCommand;
import point.ttodoApi.member.domain.Member;
import point.ttodoApi.member.infrastructure.persistence.MemberRepository;
import point.ttodoApi.profile.application.ProfileService;
import point.ttodoApi.profile.domain.Profile;
import point.ttodoApi.profile.domain.Theme;
import point.ttodoApi.profile.infrastructure.persistence.ProfileRepository;

import java.util.UUID;

import static point.ttodoApi.common.constants.SystemConstants.SystemUsers.*;

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
        Member anonMember = createSystemUser(
            ANON_USER_ID,
            ANON_USER_EMAIL,
            ANON_USER_PASSWORD,
            ANON_USER_NICKNAME,
            "시스템 익명 사용자"
        );
        
        // 루트 사용자 생성
        Member rootMember = createSystemUser(
            ROOT_USER_ID,
            ROOT_USER_EMAIL,
            ROOT_USER_PASSWORD,
            ROOT_USER_NICKNAME,
            "시스템 루트 관리자"
        );
        
        log.info("System users created: {} and {}", ANON_USER_EMAIL, ROOT_USER_EMAIL);
    }
    
    /**
     * 시드 멤버 초기화
     * @return 생성된 시드 멤버 배열
     */
    public Member[] initializeSeedMembers() {
        log.info("Initializing seed members...");
        
        Member[] members = new Member[11];
        
        // 첫 번째 멤버 (anon@ttodo.dev는 이미 시스템 사용자로 생성됨)
        Member anonMember = memberRepository.findById(ANON_USER_ID).orElseThrow();
        members[0] = anonMember;
        
        // 프로필 업데이트
        updateAnonUserProfile(anonMember);
        
        // 나머지 시드 멤버들 생성
        String[] nicknames = {
            "일일챌린저", "월간도전자", "전략적참여자", "운동매니아", "독서광",
            "습관왕", "아침형인간", "야행성참가자", "도전러", "챌린지러11"
        };
        
        for (int i = 1; i < 11; i++) {
            String email = "anon" + (i + 1) + "@ttodo.com";
            String nickname = (i - 1 < nicknames.length) ? nicknames[i - 1] : "챌린지러" + (i + 1);
            
            CreateMemberCommand command = new CreateMemberCommand(
                email,
                passwordEncoder.encode(""),
                nickname,
                null
            );
            members[i] = memberService.createMember(command);
            log.debug("Created seed member: {}", nickname);
        }
        
        log.info("Initialized {} seed members", members.length);
        return members;
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
            profile.updateIntroduction("안녕하세요! 저는 전설의소혜리입니다. 🐭 매일 꾸준히 할 일을 완료하며 성장하고 있어요!");
            profileService.saveProfile(profile);
        } catch (Exception e) {
            log.debug("Failed to update profile introduction: {}", e.getMessage());
        }
    }
}