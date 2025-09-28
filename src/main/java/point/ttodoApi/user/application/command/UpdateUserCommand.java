package point.ttodoApi.user.application.command;

import point.ttodoApi.user.domain.validation.required.ValidUserId;
import point.ttodoApi.user.domain.validation.optional.OptionalUserNickname;
// import point.ttodoApi.shared.validation.annotations.SafeHtml; // TODO: Add SafeHtml annotation

import java.util.UUID;

/**
 * 회원 정보 수정 커맨드
 * TTODO 아키텍처 패턴: Command 객체로 회원 수정 요청 캡슐화
 */
public record UpdateUserCommand(
        @ValidUserId
        UUID userId,
        
        @OptionalUserNickname
        String nickname,
        
        // @SafeHtml // TODO: Add SafeHtml validation
        String introduction // 프로필에 저장될 자기소개
) {
    public boolean hasNickname() {
        return nickname != null && !nickname.trim().isEmpty();
    }

    public boolean hasIntroduction() {
        return introduction != null;
    }
}
