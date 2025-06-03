package point.zzicback.auth.domain;

/**
 * Token 저장을 위한 Repository 인터페이스
 * DDD 원칙에 따라 도메인 계층에 위치
 * 구현체는 Infrastructure 계층에서 담당
 */
public interface TokenRepository {
  void save(String key, String value, long expirationSeconds);
  String get(String key);
  void delete(String key);
  boolean exists(String key);
}
