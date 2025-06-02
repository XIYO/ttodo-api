package point.zzicback.auth.application;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import point.zzicback.auth.config.properties.JwtProperties;
import point.zzicback.auth.domain.MemberPrincipal;
import point.zzicback.auth.repository.TokenRepository;
import point.zzicback.common.error.BusinessException;
import point.zzicback.member.application.MemberService;
import point.zzicback.member.domain.Member;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

@Service
@RequiredArgsConstructor
public class TokenService {
  public static final String DEVICE_CLAIM = "device";
  public static final String EMAIL_CLAIM = "email";
  public static final String NICKNAME_CLAIM = "nickname";
  public static final String SCOPE_CLAIM = "scope";
  public static final String SUB_CLAIM = "sub";
  
  private final JwtProperties jwtProperties;
  private final JwtEncoder jwtEncoder;
  private final JwtDecoder jwtDecoder;
  private final MemberService memberService;
  private final TokenRepository tokenRepository;

  private String generateToken(String userId, Instant expiresAt, Map<String, Object> additionalClaims) {
    Instant now = Instant.now();
    JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder().subject(userId).issuedAt(now).expiresAt(expiresAt);
    additionalClaims.forEach(claimsBuilder::claim);
    JwtClaimsSet claims = claimsBuilder.build();
    JwtEncoderParameters parameters = JwtEncoderParameters
            .from(JwsHeader.with(() -> "RS256").keyId(jwtProperties.keyId()).build(), claims);
    return jwtEncoder.encode(parameters).getTokenValue();
  }

  public String generateAccessToken(String id, String email, String nickname) {
    Instant expiresAt = Instant.now().plus(jwtProperties.expiration(), ChronoUnit.SECONDS);
    Map<String, Object> claims = Map.of(
      EMAIL_CLAIM, email, 
      NICKNAME_CLAIM, nickname, 
      SCOPE_CLAIM, "ROLE_USER"
    );
    return generateToken(id, expiresAt, claims);
  }

  public String generateRefreshToken(String id, String device) {
    Instant expiresAt = Instant.now().plus(jwtProperties.refreshExpiration(), ChronoUnit.SECONDS);
    Map<String, Object> claims = Map.of(DEVICE_CLAIM, device);
    return generateToken(id, expiresAt, claims);
  }

  public String extractClaim(String token, String claimName) {
    try {
      String[] parts = token.split("\\.");
      if (parts.length < 2) {
        throw new IllegalArgumentException("Invalid JWT format");
      }
      String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
      String target = "\"" + claimName + "\"";
      int start = payloadJson.indexOf(target);
      if (start == -1)
        return null;
      int colon = payloadJson.indexOf(':', start);
      int valueStart = payloadJson.indexOf('"', colon + 1) + 1;
      int valueEnd = payloadJson.indexOf('"', valueStart);
      return payloadJson.substring(valueStart, valueEnd);
    } catch (Exception e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "토큰 파싱 실패", e);
    }
  }

  public boolean isValidToken(String token) {
    try {
      jwtDecoder.decode(token);
      return true;
    } catch (JwtException _) {
      return false;
    }
  }

  public void save(String deviceId, String refreshToken) {
    long refreshSeconds = jwtProperties.refreshExpiration();
    tokenRepository.save(deviceId, refreshToken, refreshSeconds);
  }

  public void deleteByToken(String refreshToken) {
    String deviceId = extractClaim(refreshToken, DEVICE_CLAIM);
    tokenRepository.delete(deviceId);
  }

  public boolean isValidRefreshToken(String deviceId, String refreshToken) {
    String storedToken = tokenRepository.get(deviceId);
    return refreshToken.equals(storedToken);
  }

  public TokenPair refreshTokens(String deviceId, String oldRefreshToken) {
    validateAndDeleteInvalidToken(deviceId, oldRefreshToken);
    
    UUID memberId = UUID.fromString(extractClaim(oldRefreshToken, SUB_CLAIM));
    Member member = memberService.findVerifiedMember(memberId);
    
    String newAccessToken = generateAccessToken(memberId.toString(), member.getEmail(), member.getNickname());
    String newRefreshToken = generateRefreshToken(memberId.toString(), deviceId);
    save(deviceId, newRefreshToken);
    
    return new TokenPair(newAccessToken, newRefreshToken);
  }

  private void validateAndDeleteInvalidToken(String deviceId, String refreshToken) {
    if (!isValidToken(refreshToken) || !isValidRefreshToken(deviceId, refreshToken)) {
      tokenRepository.delete(deviceId);
      throw new BusinessException("Invalid refresh token");
    }
  }

  public record TokenPair(
          @Schema(description = "액세스 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...") String accessToken,
          @Schema(description = "리프레시 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...") String refreshToken) {
  }

  public TokenResult generateTokens(MemberPrincipal member) {
    String deviceId = UUID.randomUUID().toString();
    String accessToken = generateAccessToken(member.idAsString(), member.email(), member.nickname());
    String refreshToken = generateRefreshToken(member.idAsString(), deviceId);
    save(deviceId, refreshToken);
    return new TokenResult(accessToken, refreshToken, deviceId);
  }



  public record TokenResult(String accessToken, String refreshToken, String deviceId) {
  }
}
