package point.zzicback.auth.infrastructure.persistence;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;
import point.zzicback.auth.domain.TokenRepository;

import java.time.LocalDateTime;
import java.util.concurrent.*;

/**
 * InMemory 기반 TokenRepository 구현체
 * Infrastructure 계층에 위치 - 기술적 구현사항을 담당
 * 개발/테스트 환경에서 사용하는 Infrastructure 컴포넌트
 */
@Repository
@Profile("!redis")
public class InMemoryTokenRepository implements TokenRepository {
  private final ConcurrentMap<String, TokenEntry> store = new ConcurrentHashMap<>();

  @Override
  public void save(String key, String value, long expirationSeconds) {
    LocalDateTime expiresAt = LocalDateTime.now().plusSeconds(expirationSeconds);
    store.put(key, new TokenEntry(value, expiresAt));
  }

  @Override
  public String get(String key) {
    TokenEntry entry = store.get(key);
    if (entry == null || LocalDateTime.now().isAfter(entry.expiresAt())) {
      store.remove(key);
      return null;
    }
    return entry.value();
  }

  @Override
  public void delete(String key) {
    store.remove(key);
  }

  @Override
  public boolean exists(String key) {
    TokenEntry entry = store.get(key);
    boolean exists = entry != null && LocalDateTime.now().isBefore(entry.expiresAt());
    if (!exists) {
      store.remove(key);
    }
    return exists;
  }

  private record TokenEntry(String value, LocalDateTime expiresAt) {
  }
}
