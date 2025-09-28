package point.ttodoApi.auth.application.command;

import point.ttodoApi.auth.domain.validation.required.ValidEmail;
import point.ttodoApi.auth.domain.validation.required.ValidDeviceId;

/**
 * 로그인 커맨드
 * TTODO 아키텍처 패턴: Command 객체로 로그인 요청 캡슐화
 */
public record SignInCommand(
        @ValidEmail
        String email,
        
        String password,
        
        @ValidDeviceId
        String deviceId
) {

}