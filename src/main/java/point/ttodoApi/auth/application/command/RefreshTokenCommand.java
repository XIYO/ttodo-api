package point.ttodoApi.auth.application.command;

import point.ttodoApi.auth.domain.validation.required.ValidDeviceId;
import jakarta.validation.constraints.NotBlank;

/**
 * 토큰 갱신 커맨드
 * TTODO 아키텍처 패턴: Command 객체로 토큰 갱신 요청 캡슐화
 */
public record RefreshTokenCommand(
        @ValidDeviceId
        String deviceId,
        
        @NotBlank(message = "리프레시 토큰은 필수입니다")
        String refreshToken
) {
}