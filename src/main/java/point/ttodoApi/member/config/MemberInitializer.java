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
import point.ttodoApi.todo.config.TodoInitializer;

import static point.ttodoApi.common.constants.SystemConstants.SystemUsers.*;

/**
 * 멤버 관련 데이터 초기화
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
@org.springframework.context.annotation.Profile("!test")
public class MemberInitializer implements ApplicationRunner {
    
    private final MemberRepository memberRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final MemberService memberService;
    private final ProfileService profileService;
    private final TodoInitializer todoInitializer;
    private final java.util.Random random = new java.util.Random();
    
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        log.info("Starting member initialization...");
        initializeSystemUsers();
        Member[] seedMembers = createSeedMembers();
        log.info("Member initialization completed!");
    }
    
    private void initializeSystemUsers() {
        // 익명 사용자 생성
        if (!memberRepository.existsById(ANON_USER_ID)) {
            Member anonMember = Member.builder()
                    .email(ANON_USER_EMAIL)
                    .password(passwordEncoder.encode(ANON_USER_PASSWORD))
                    .nickname(ANON_USER_NICKNAME)
                    .build();
            anonMember.setId(ANON_USER_ID);
            memberRepository.save(anonMember);
            
            // 프로필 생성
            Profile anonProfile = Profile.builder()
                    .ownerId(ANON_USER_ID)
                    .introduction("시스템 익명 사용자")
                    .theme(Theme.PINKY)
                    .timeZone("Asia/Seoul")
                    .locale("ko-KR")
                    .build();
            profileRepository.save(anonProfile);
            
            log.info("익명 사용자 생성 완료: {}", ANON_USER_EMAIL);
        }
        
        // 루트 사용자 생성
        if (!memberRepository.existsById(ROOT_USER_ID)) {
            Member rootMember = Member.builder()
                    .email(ROOT_USER_EMAIL)
                    .password(passwordEncoder.encode(ROOT_USER_PASSWORD))
                    .nickname(ROOT_USER_NICKNAME)
                    .build();
            rootMember.setId(ROOT_USER_ID);
            memberRepository.save(rootMember);
            
            // 프로필 생성
            Profile rootProfile = Profile.builder()
                    .ownerId(ROOT_USER_ID)
                    .introduction("시스템 루트 관리자")
                    .theme(Theme.PINKY)
                    .timeZone("Asia/Seoul")
                    .locale("ko-KR")
                    .build();
            profileRepository.save(rootProfile);
            
            log.info("루트 사용자 생성 완료: {}", ROOT_USER_EMAIL);
        }
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
    
    public Member[] getCreatedMembers() {
        return createSeedMembers();
    }
}