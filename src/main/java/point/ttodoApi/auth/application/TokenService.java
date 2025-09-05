package point.ttodoApi.auth.application;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import point.ttodoApi.auth.config.properties.JwtProperties;
import point.ttodoApi.auth.domain.*;
import point.ttodoApi.shared.error.BusinessException;
import point.ttodoApi.member.application.MemberService;
import point.ttodoApi.member.domain.Member;
import point.ttodoApi.profile.application.ProfileService;
import point.ttodoApi.profile.domain.Profile;

import jakarta.annotation.PreDestroy;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
  
  // 동시 리프레시 요청을 위한 캐시 (10초간 유지, 최대 1000개)
  private final Map<String, TokenPair> recentRefreshCache = new ConcurrentHashMap<>();
  private final ScheduledExecutorService cleanupExecutor = Executors.newSingleThreadScheduledExecutor();
  
  // deviceId별 lock 관리 (String.intern() 대체)
  private final Map<String, Lock> deviceLocks = new ConcurrentHashMap<>();
  private static final int MAX_LOCKS = 10000; // 최대 lock 개수 제한

  /**
   * JWT 토큰을 생성합니다.
   * 
   * @param userId 사용자 ID
   * @param expiresAt 토큰 만료 시간
   * @param additionalClaims 추가 클레임
   * @return 생성된 JWT 토큰 문자열
   */
  private String generateToken(String userId, Instant expiresAt, Map<String, Object> additionalClaims) {
    Instant now = Instant.now();
    JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
            .subject(userId)
            .issuedAt(now)
            .expiresAt(expiresAt);
    
    additionalClaims.forEach(claimsBuilder::claim);
    JwtClaimsSet claims = claimsBuilder.build();
    JwtEncoderParameters parameters = JwtEncoderParameters
            .from(JwsHeader.with(() -> "RS256").keyId(jwtProperties.keyId()).build(), claims);
    return jwtEncoder.encode(parameters).getTokenValue();
  }
  
  /**
   * 만료 시간이 없는 JWT 토큰을 생성합니다.
   * 개발 및 테스트 환경에서만 사용해야 합니다.
   * 
   * @param userId 사용자 ID
   * @param additionalClaims 추가 클레임
   * @return 만료되지 않는 JWT 토큰 문자열
   */
  private String generateToken(String userId, Map<String, Object> additionalClaims) {
    Instant now = Instant.now();
    JwtClaimsSet.Builder claimsBuilder = JwtClaimsSet.builder()
            .subject(userId)
            .issuedAt(now);
    // expiresAt을 설정하지 않음 - 만료 없음
    
    additionalClaims.forEach(claimsBuilder::claim);
    JwtClaimsSet claims = claimsBuilder.build();
    JwtEncoderParameters parameters = JwtEncoderParameters
            .from(JwsHeader.with(() -> "RS256").keyId(jwtProperties.keyId()).build(), claims);
    return jwtEncoder.encode(parameters).getTokenValue();
  }

  /**
   * 액세스 토큰을 생성합니다.
   * 
   * @param id 사용자 ID
   * @param email 사용자 이메일
   * @param nickname 사용자 닉네임
   * @param timeZone 사용자 타임존
   * @param locale 사용자 로케일
   * @return 생성된 액세스 토큰
   */
  public String generateAccessToken(String id, String email, String nickname, String timeZone, String locale) {
    return generateAccessToken(id, email, nickname, timeZone, locale, false);
  }
  
  /**
   * 액세스 토큰을 생성합니다.
   * 
   * @param id 사용자 ID
   * @param email 사용자 이메일
   * @param nickname 사용자 닉네임
   * @param timeZone 사용자 타임존
   * @param locale 사용자 로케일
   * @param neverExpire true인 경우 만료되지 않는 토큰 생성 (개발/테스트용)
   * @return 생성된 액세스 토큰
   */
  public String generateAccessToken(String id, String email, String nickname, String timeZone, String locale, boolean neverExpire) {
    Map<String, Object> claims = Map.of(
      EMAIL_CLAIM, email,
      NICKNAME_CLAIM, nickname,
      TIME_ZONE_CLAIM, timeZone,
      LOCALE_CLAIM, locale,
      SCOPE_CLAIM, "ROLE_USER"
    );
    
    if (neverExpire) {
      // 만료 없는 토큰 생성 (개발/테스트 환경용)
      return generateToken(id, claims);
    } else {
      // 일반 토큰 생성 (만료시간 있음)
      Instant expiresAt = Instant.now().plus(jwtProperties.accessToken().expiration(), ChronoUnit.SECONDS);
      return generateToken(id, expiresAt, claims);
    }
  }
  

  /**
   * 리프레시 토큰을 생성합니다.
   * 
   * @param id 사용자 ID
   * @param device 디바이스 ID
   * @return 생성된 리프레시 토큰
   */
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
    
    // 동일 디바이스에 대한 동시 요청 방지 (String.intern() 대신 안전한 lock 사용)
    Lock lock = getOrCreateLock(deviceId);
    lock.lock();
    try {
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
      
      // 캐시에 저장하고 10초 후 자동 제거 (캐시 크기 제한 확인)
      if (recentRefreshCache.size() < 1000) {
        recentRefreshCache.put(cacheKey, newTokens);
        cleanupExecutor.schedule(() -> {
          recentRefreshCache.remove(cacheKey);
          log.debug("Removed cached token for key: {}", cacheKey);
        }, 10, TimeUnit.SECONDS);
      }
      
      return newTokens;
    } finally {
      lock.unlock();
      // Lock 개수가 너무 많아지면 오래된 것 정리
      cleanupLocksIfNeeded();
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

  
  /**
   * deviceId에 대한 Lock을 가져오거나 생성
   * String.intern() 대신 안전한 lock 관리 방식
   */
  private Lock getOrCreateLock(String deviceId) {
    return deviceLocks.computeIfAbsent(deviceId, k -> new ReentrantLock());
  }
  
  /**
   * Lock 개수가 너무 많아지면 오래된 것 정리
   * 메모리 누수 방지
   */
  private void cleanupLocksIfNeeded() {
    if (deviceLocks.size() > MAX_LOCKS) {
      // 비동기로 정리 작업 수행
      cleanupExecutor.execute(() -> {
        try {
          // 사용되지 않는 lock 제거 (tryLock이 성공하는 것들)
          deviceLocks.entrySet().removeIf(entry -> {
            Lock lock = entry.getValue();
            if (lock instanceof ReentrantLock reentrantLock) {
              // Lock이 사용 중이지 않으면 제거
              if (reentrantLock.tryLock()) {
                try {
                  return !reentrantLock.hasQueuedThreads();
                } finally {
                  reentrantLock.unlock();
                }
              }
            }
            return false;
          });
          log.info("Cleaned up device locks. Current size: {}", deviceLocks.size());
        } catch (Exception e) {
          log.error("Error during lock cleanup", e);
        }
      });
    }
  }
  
  /**
   * 애플리케이션 종료 시 리소스 정리
   */
  @PreDestroy
  public void cleanup() {
    log.info("Shutting down TokenService executor");
    cleanupExecutor.shutdown();
    try {
      if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
        cleanupExecutor.shutdownNow();
      }
    } catch (InterruptedException e) {
      cleanupExecutor.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }

  public record TokenResult(String accessToken, String refreshToken, String deviceId) {
  }
}
