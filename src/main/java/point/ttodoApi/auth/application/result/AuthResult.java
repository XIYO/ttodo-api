package point.ttodoApi.auth.application.result;

import java.util.UUID;

/**
 * 인증 결과 (TTODO 아키텍처 패턴)
 * Application Layer의 결과 객체
 */
public record AuthResult(
        String accessToken,
        String refreshToken,
        String deviceId,
        UUID userId,
        String email,
        String nickname
) {
    /**
     * 토큰 결과만 포함한 생성자 (개발용 토큰)
     */
    public static AuthResult ofTokenOnly(String accessToken, String refreshToken, String deviceId) {
        return new AuthResult(accessToken, refreshToken, deviceId, null, null, null);
    }
}