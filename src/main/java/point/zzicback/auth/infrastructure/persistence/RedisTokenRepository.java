package point.zzicback.auth.infrastructure.persistence;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;
import point.zzicback.auth.domain.TokenRepository;

import java.util.concurrent.TimeUnit;

/**
 * Redis 기반 TokenRepository 구현체
 * Infrastructure 계층에 위치 - 기술적 구현사항을 담당
 * 프로덕션 환경에서 사용하는 Infrastructure 컴포넌트
 */
@Repository
@RequiredArgsConstructor
@Profile("redis")
public class RedisTokenRepository implements TokenRepository {
  private final RedisTemplate<String, String> redisTemplate;

  @Override
  public void save(String key, String value, long expirationSeconds) {
    redisTemplate.opsForValue().set(key, value, expirationSeconds, TimeUnit.SECONDS);
  }

  @Override
  public String get(String key) {
    return redisTemplate.opsForValue().get(key);
  }

  @Override
  public void delete(String key) {
    redisTemplate.delete(key);
  }

  @Override
  public boolean exists(String key) {
    return redisTemplate.hasKey(key);
  }
}
