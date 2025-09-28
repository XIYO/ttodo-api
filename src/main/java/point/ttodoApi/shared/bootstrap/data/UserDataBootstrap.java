package point.ttodoApi.shared.bootstrap.data;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import point.ttodoApi.user.domain.User;
import point.ttodoApi.user.infrastructure.persistence.UserRepository;
import point.ttodoApi.profile.domain.Profile;
import point.ttodoApi.profile.domain.Theme;
import point.ttodoApi.profile.infrastructure.persistence.ProfileRepository;
import point.ttodoApi.shared.config.properties.AppProperties;

import java.util.UUID;

import static point.ttodoApi.shared.constants.SystemConstants.SystemUsers.*;

/**
 * 시스템 사용자 초기 데이터 생성
 * 익명 사용자, 루트 사용자 생성
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserDataBootstrap {

    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties appProperties;

    public void initialize() {
        if (userRepository.existsById(ANON_USER_ID)) {
            log.info("System users already exist, skipping user initialization");
            return;
        }

        String domain = appProperties.getUserDomain();
        
        // 익명 사용자 생성
        createSystemUser(
                ANON_USER_ID,
                "anon@" + domain,
                ANON_USER_PASSWORD,
                ANON_USER_NICKNAME,
                "시스템 익명 사용자"
        );

        // 루트 사용자 생성
        createSystemUser(
                ROOT_USER_ID,
                "root@" + domain,
                ROOT_USER_PASSWORD,
                ROOT_USER_NICKNAME,
                "시스템 루트 관리자"
        );

        log.info("System users created: anon@{} and root@{}", domain, domain);
    }

    /**
     * 시스템 사용자 생성 (멤버 + 프로필)
     */
    private void createSystemUser(UUID id, String email, String password, String nickname, String introduction) {
        // 멤버 생성 (nickname 필드 제거됨)
        User user = User.builder()
                .id(id)
                .email(email)
                .password(passwordEncoder.encode(password))
                .build();
        user = userRepository.saveAndFlush(user);  // Ensure user is persisted and flushed

        // 프로필 생성
        Profile profile = Profile.builder()
                .owner(user)
                .nickname(nickname)
                .introduction(introduction)
                .theme(Theme.PINKY)
                .timeZone("Asia/Seoul")
                .locale("ko-KR")
                .build();
        profileRepository.saveAndFlush(profile);  // Ensure profile is persisted and flushed

        log.debug("Created system user: {} ({})", nickname, email);
    }
}
