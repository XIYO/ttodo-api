package point.ttodoApi.profile.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import point.ttodoApi.profile.application.command.UpdateProfileCommand;
import point.ttodoApi.profile.domain.Profile;
import point.ttodoApi.profile.domain.Theme;

import jakarta.validation.Valid;

/**
 * Profile Command Service
 * TTODO 아키텍처 패턴: Command 처리 전용 서비스
 */
@Service
@RequiredArgsConstructor
@Transactional
@Validated
public class ProfileCommandService {
    
    private final ProfileService profileService; // 기존 서비스 위임

    /**
     * 프로필 수정
     */
    public Profile updateProfile(@Valid UpdateProfileCommand command) {
        // 기존 서비스로 위임 (향후 TTODO 아키텍처 패턴으로 리팩토링)
        Profile profile = profileService.getProfile(command.userId());
        
        // Profile 엔티티에 nickname 필드가 없으므로 생략
        // if (command.nickname() != null) {
        //     profile.updateNickname(command.nickname());
        // }
        
        if (command.introduction() != null) {
            profile.setIntroduction(command.introduction());
        }
        if (command.timeZone() != null) {
            profile.setTimeZone(command.timeZone().toString());
        }
        if (command.locale() != null) {
            profile.setLocale(command.locale().toString());
        }
        if (command.theme() != null) {
            // String을 Theme enum으로 변환
            try {
                Theme theme = Theme.valueOf(command.theme().toUpperCase());
                profile.setTheme(theme);
            } catch (IllegalArgumentException e) {
                // 잘못된 테마명인 경우 기본 테마 사용
                profile.setTheme(Theme.PINKY);
            }
        }
        
        return profileService.saveProfile(profile);
    }
}