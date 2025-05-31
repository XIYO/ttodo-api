package point.zzicback.auth.jwt;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import point.zzicback.auth.config.properties.JwtProperties;
import point.zzicback.auth.repository.TokenRepository;
import point.zzicback.auth.util.JwtUtil;
import point.zzicback.member.application.MemberService;
import point.zzicback.member.domain.Member;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;
    private final MemberService memberService;
    private final TokenRepository tokenRepository;

    public void save(String deviceId, String refreshToken) {
        long refreshSeconds = jwtProperties.refreshExpiration();
        tokenRepository.save(deviceId, refreshToken, refreshSeconds);
    }

    public void deleteByToken(String refreshToken) {
        String deviceId = jwtUtil.extractClaim(refreshToken, "device");
        tokenRepository.delete(deviceId);
    }

    public boolean isValidRefreshToken(String deviceId, String refreshToken) {
        String storedToken = tokenRepository.get(deviceId);
        return refreshToken.equals(storedToken);
    }

    public record TokenPair(
            @Schema(description = "액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            String accessToken,
            @Schema(description = "리프레시 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
            String refreshToken
    ) {}

    public TokenPair refreshTokens(String deviceId, String oldRefreshToken) {
        UUID memberId = UUID.fromString(jwtUtil.extractClaim(oldRefreshToken, "sub"));
        Member member = memberService.findVerifiedMember(memberId);

        String newAccessToken = jwtUtil.generateAccessToken(memberId.toString(), member.getEmail(), member.getNickname());
        String newRefreshToken = jwtUtil.generateRefreshToken(memberId.toString(), deviceId);

        save(deviceId, newRefreshToken);
        return new TokenPair(newAccessToken, newRefreshToken);
    }
}
