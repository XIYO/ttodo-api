package point.ttodoApi.test;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import point.ttodoApi.auth.application.TokenService;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 테스트용 JWT 토큰을 생성하고 출력하는 테스트
 * 이 테스트를 실행하여 생성된 토큰을 복사해서 다른 테스트에서 하드코딩하여 사용합니다.
 */
@Slf4j
@SpringBootTest
class GenerateTestTokenTest extends BaseIntegrationTest {

  @Autowired
  private TokenService tokenService;

  @Test
  void generateAndPrintAnonToken() {
    // TokenService의 generateAccessToken 메서드에 neverExpire 옵션 사용
    String token = tokenService.generateAccessToken(
            "ffffffff-ffff-ffff-ffff-ffffffffffff",
            "anon@ttodo.dev",
            "익명사용자",
            "Asia/Seoul",
            "ko-KR",
            true  // neverExpire = true
    );

    log.info("\n================================================================================");
    log.info("=== ANON USER TEST TOKEN (Copy this for hardcoding in tests) ===");
    log.info("================================================================================");
    log.info("Generated Token: {}", token);
    log.info("================================================================================\n");

    // 토큰이 생성되었는지 확인
    assertThat(token).isNotNull();
    assertThat(token).startsWith("eyJ");  // JWT는 항상 eyJ로 시작
  }
}