package point.ttodoApi.common.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import point.ttodoApi.member.domain.Member;
import point.ttodoApi.member.infrastructure.persistence.MemberRepository;
import point.ttodoApi.profile.domain.Profile;
import point.ttodoApi.profile.domain.Theme;
import point.ttodoApi.profile.infrastructure.persistence.ProfileRepository;

import static point.ttodoApi.common.constants.SystemConstants.SystemUsers.*;

/**
 * 시스템 시작 시 필수 데이터 초기화
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
@org.springframework.context.annotation.Profile("!test")
public class SystemDataInitializer implements ApplicationRunner {
    
    private final MemberRepository memberRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        initializeSystemUsers();
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
}