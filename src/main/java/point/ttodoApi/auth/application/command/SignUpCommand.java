package point.ttodoApi.auth.application.command;

import point.ttodoApi.auth.domain.validation.required.ValidEmail;
import point.ttodoApi.auth.domain.validation.required.ValidNickname;
import point.ttodoApi.auth.domain.validation.required.ValidDeviceId;
import point.ttodoApi.auth.domain.validation.optional.OptionalIntroduction;
import point.ttodoApi.shared.validation.annotations.SanitizeHtml;

/**
 * 회원가입 커맨드
 * TTODO 아키텍처 패턴: Command 객체로 회원가입 요청 캡슐화
 */
public record SignUpCommand(
        @ValidEmail
        String email,
        
        String password,
        
        @ValidNickname
        String nickname,
        
        @OptionalIntroduction
        String introduction,
        
        @ValidDeviceId
        String deviceId
) {

}