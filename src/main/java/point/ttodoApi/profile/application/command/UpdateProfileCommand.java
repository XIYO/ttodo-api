package point.ttodoApi.profile.application.command;

import point.ttodoApi.user.domain.validation.required.ValidUserId;
import point.ttodoApi.profile.domain.validation.optional.OptionalProfileIntroduction;
import point.ttodoApi.profile.domain.validation.optional.OptionalProfileNickname;

import java.time.ZoneId;
import java.util.Locale;
import java.util.UUID;

/**
 * 프로필 수정 커맨드
 * TTODO 아키텍처 패턴: Command 객체로 프로필 수정 요청 캡슐화
 */
public record UpdateProfileCommand(
        @ValidUserId
        UUID userId,
        
        @OptionalProfileNickname
        String nickname,
        
        @OptionalProfileIntroduction
        String introduction,
        
        ZoneId timeZone,
        
        Locale locale,
        
        String theme
) {
}