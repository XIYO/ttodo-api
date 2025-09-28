package point.ttodoApi.user.application.command;

import org.springframework.lang.Nullable;
import point.ttodoApi.user.domain.validation.ValidPassword;
import point.ttodoApi.user.domain.validation.required.*;

/**
 * 회원 생성 커맨드
 * TTODO 아키텍처 패턴: Command 객체로 회원 생성 요청 캡슐화
 */
public record CreateUserCommand(
        @ValidUserEmail
        String email,

        @ValidPassword
        String password,

        @ValidUserNickname
        String nickname,

        @Nullable
        String introduction // 프로필에 저장될 자기소개
) {
}
