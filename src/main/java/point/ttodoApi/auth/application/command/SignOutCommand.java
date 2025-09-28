package point.ttodoApi.auth.application.command;

import point.ttodoApi.auth.domain.validation.required.ValidDeviceId;
import jakarta.validation.constraints.NotBlank;

/**
 * 로그아웃 커맨드
 * TTODO 아키텍처 패턴: Command 객체로 로그아웃 요청 캡슐화
 */
public record SignOutCommand(
        @ValidDeviceId
        String deviceId,
        
        @NotBlank(message = "리프레시 토큰은 필수입니다")
        String refreshToken
) {
}