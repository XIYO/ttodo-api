package point.zzicback.common.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import point.zzicback.common.properties.JwtProperties;
import point.zzicback.common.utill.JwtUtil;
import point.zzicback.member.application.MemberService;
import point.zzicback.member.application.dto.response.MemberMeResponse;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;
    private final MemberService memberService;

    private final RedisTemplate<String, String> redisTemplate;

    public void save(String deviceId, String refreshToken) {
        long refreshSeconds = jwtProperties.refreshExpiration();
        redisTemplate.opsForValue().set(deviceId, refreshToken, refreshSeconds, TimeUnit.SECONDS);
    }

    public void deleteByToken(String refreshToken) {
        String deviceId = jwtUtil.extractClaim(refreshToken, "device");
        redisTemplate.delete(deviceId);
    }

    public record TokenPair(String accessToken, String refreshToken) {}

    public TokenPair refreshTokens(String deviceId, String oldRefreshToken) {
        // 리프레시 토큰에서 멤버 ID(sub) 추출
        UUID memberId = UUID.fromString(jwtUtil.extractClaim(oldRefreshToken, "sub"));
        MemberMeResponse me = memberService.getMemberMe(memberId);

        // 새 액세스 토큰과 리프레시 토큰 발급
        String newAccessToken = jwtUtil.generateAccessToken(memberId.toString(), me.email(), me.nickname());
        String newRefreshToken = jwtUtil.generateRefreshToken(memberId.toString(), deviceId);

        // 저장
        save(deviceId, newRefreshToken);
        return new TokenPair(newAccessToken, newRefreshToken);
    }
}
