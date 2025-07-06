package point.zzicback.auth.application;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import java.util.concurrent.*;
import point.zzicback.auth.config.properties.JwtProperties;
import point.zzicback.auth.domain.*;
import point.zzicback.common.error.BusinessException;
import point.zzicback.member.application.MemberService;
import point.zzicback.member.domain.Member;
import point.zzicback.profile.application.ProfileService;
import point.zzicback.profile.domain.Profile;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenService {
  public static final String DEVICE_CLAIM = "device";
  public static final String EMAIL_CLAIM = "email";
  public static final String NICKNAME_CLAIM = "nickname";
  public static final String TIME_ZONE_CLAIM = "timeZone";
  public static final String LOCALE_CLAIM = "locale";
  public static final String SCOPE_CLAIM = "scope";
  public static final String SUB_CLAIM = "sub";
  
  private final JwtProperties jwtProperties;
  private final JwtEncoder jwtEncoder;
  private final JwtDecoder jwtDecoder;
  private final MemberService memberService;
  private final ProfileService profileService;
  private final TokenRepository tokenRepository;
  
  // 동시 리프레시 요청을 위한 캐시 (10초간 유지)
  private final Map<String, TokenPair> recentRefreshCache = new ConcurrentHashMap<>();
  private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();

  private String generateToken(String userId, Instant expiresAt, Map<String, Object> additionalClaims) {
    Instant now = Instant.now();
    JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder().subject(userId).issuedAt(now).expiresAt(expiresAt);
    additionalClaims.forEach(claimsBuilder::claim);
    JwtClaimsSet claims = claimsBuilder.build();
    JwtEncoderParameters parameters = JwtEncoderParameters
            .from(JwsHeader.with(() -> "RS256").keyId(jwtProperties.keyId()).build(), claims);
    return jwtEncoder.encode(parameters).getTokenValue();
  }

  public String generateAccessToken(String id, String email, String nickname, String timeZone, String locale) {
    Instant expiresAt = Instant.now().plus(jwtProperties.accessToken().expiration(), ChronoUnit.SECONDS);
    Map<String, Object> claims = Map.of(
      EMAIL_CLAIM, email,
      NICKNAME_CLAIM, nickname,
      TIME_ZONE_CLAIM, timeZone,
      LOCALE_CLAIM, locale,
      SCOPE_CLAIM, "ROLE_USER"
    );
    return generateToken(id, expiresAt, claims);
  }

  public String generateRefreshToken(String id, String device) {
    Instant expiresAt = Instant.now().plus(jwtProperties.refreshToken().expiration(), ChronoUnit.SECONDS);
    Map<String, Object> claims = Map.of(DEVICE_CLAIM, device);
    return generateToken(id, expiresAt, claims);
  }

  public String extractClaim(String token, String claimName) {
    try {
      String[] parts = token.split("\\.");
      if (parts.length < 2) {
        log.warn("Invalid JWT format: insufficient parts");
        return null;
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
      log.warn("Failed to extract claim '{}' from token: {}", claimName, e.getMessage());
      return null;
    }
  }

  public boolean isValidToken(String token) {
    try {
      jwtDecoder.decode(token);
      return true;
    } catch (JwtException e) {
      return false;
    }
  }

  public void save(String deviceId, String refreshToken) {
    long refreshSeconds = jwtProperties.refreshToken().expiration();
    tokenRepository.save(deviceId, refreshToken, refreshSeconds);
  }

  public void deleteByToken(String refreshToken) {
    try {
      String deviceId = extractClaim(refreshToken, DEVICE_CLAIM);
      if (deviceId != null) {
        tokenRepository.delete(deviceId);
      } else {
        log.warn("Could not extract device ID from refresh token for deletion");
      }
    } catch (Exception e) {
      log.warn("Failed to delete token: {}", e.getMessage());
    }
  }

  public boolean isValidRefreshToken(String deviceId, String refreshToken) {
    String storedToken = tokenRepository.get(deviceId);
    return refreshToken.equals(storedToken);
  }

  public TokenPair refreshTokens(String deviceId, String oldRefreshToken) {
    // 10초 이내에 동일한 리프레시 토큰으로 요청이 왔다면 캐시된 결과 반환
    String cacheKey = deviceId + ":" + oldRefreshToken.hashCode();
    TokenPair cached = recentRefreshCache.get(cacheKey);
    if (cached != null) {
      log.info("Returning cached token for concurrent refresh request: deviceId={}", deviceId);
      return cached;
    }
    
    // 동일 디바이스에 대한 동시 요청 방지
    synchronized (deviceId.intern()) {
      // Double-check: 다른 스레드가 방금 처리했을 수 있음
      cached = recentRefreshCache.get(cacheKey);
      if (cached != null) {
        return cached;
      }
      
      validateAndDeleteInvalidToken(deviceId, oldRefreshToken);
      
      UUID memberId = UUID.fromString(extractClaim(oldRefreshToken, SUB_CLAIM));
      Member member = memberService.findVerifiedMember(memberId);
      Profile profile = profileService.getProfile(memberId);
      
      String newAccessToken = generateAccessToken(
              memberId.toString(),
              member.getEmail(),
              member.getNickname(),
              profile.getTimeZone(),
              profile.getLocale());
      String newRefreshToken = generateRefreshToken(memberId.toString(), deviceId);
      save(deviceId, newRefreshToken);
      
      TokenPair newTokens = new TokenPair(newAccessToken, newRefreshToken);
      
      // 캐시에 저장하고 10초 후 자동 제거
      recentRefreshCache.put(cacheKey, newTokens);
      cleanupExecutor.schedule(() -> {
        recentRefreshCache.remove(cacheKey);
        log.debug("Removed cached token for key: {}", cacheKey);
      }, 10, TimeUnit.SECONDS);
      
      return newTokens;
    }
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
    Profile profile = profileService.getProfile(member.id());
    String accessToken = generateAccessToken(
            member.idAsString(),
            member.email(),
            member.nickname(),
            profile.getTimeZone(),
            profile.getLocale());
    String refreshToken = generateRefreshToken(member.idAsString(), deviceId);
    save(deviceId, refreshToken);
    return new TokenResult(accessToken, refreshToken, deviceId);
  }



  public record TokenResult(String accessToken, String refreshToken, String deviceId) {
  }
}
