package point.zzicback.auth.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("dev")
@Transactional
class H2TokenRepositoryTest {
  @Autowired
  private TokenRepository tokenRepository;

  @Test
  void 토큰_저장_및_조회_테스트() {
    // Given
    String key = "test-refresh-token";
    String value = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.test";
    long timeoutSeconds = 3600; // 1시간
    // When
    tokenRepository.save(key, value, timeoutSeconds);
    // Then
    assertThat(tokenRepository.exists(key)).isTrue();
    assertThat(tokenRepository.get(key)).isEqualTo(value);
  }

  @Test
  void 토큰_삭제_테스트() {
    // Given
    String key = "test-token-to-delete";
    String value = "test-value";
    tokenRepository.save(key, value, 3600);
    // When
    tokenRepository.delete(key);
    // Then
    assertThat(tokenRepository.exists(key)).isFalse();
    assertThat(tokenRepository.get(key)).isNull();
  }

  @Test
  void 만료된_토큰은_조회되지_않음() {
    // Given
    String key = "expired-token";
    String value = "test-value";
    long expiredTimeoutSeconds = -1; // 이미 만료
    // When
    tokenRepository.save(key, value, expiredTimeoutSeconds);
    // Then
    assertThat(tokenRepository.exists(key)).isFalse();
    assertThat(tokenRepository.get(key)).isNull();
  }
}
