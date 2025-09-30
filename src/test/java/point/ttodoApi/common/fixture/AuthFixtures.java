package point.ttodoApi.common.fixture;

import point.ttodoApi.auth.application.result.AuthResult;

import java.util.UUID;

public record AuthFixtures() {
    
    public static AuthResult createAuthResult() {
        return new AuthResult(
            "test-access-token",
            "test-refresh-token",
            "test-device-id",
            UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff"),
            "test@example.com",
            "테스트유저"
        );
    }
    
    public static AuthResult createAuthResult(String accessToken, String refreshToken) {
        return new AuthResult(
            accessToken, 
            refreshToken,
            "test-device-id",
            UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff"),
            "test@example.com",
            "테스트유저"
        );
    }
    
    public static AuthResult createTokenOnly(String accessToken, String refreshToken, String deviceId) {
        return AuthResult.ofTokenOnly(accessToken, refreshToken, deviceId);
    }
    
    public record TestUser(
        UUID id,
        String email,
        String nickname
    ) {
        public static TestUser createDefault() {
            return new TestUser(
                UUID.fromString("ffffffff-ffff-ffff-ffff-ffffffffffff"),
                "test@example.com",
                "테스트유저"
            );
        }
        
        public static TestUser create(String email, String nickname) {
            return new TestUser(
                UUID.randomUUID(),
                email,
                nickname
            );
        }
    }
}