package point.ttodoApi.common.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.security.*;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;

import java.util.List;

@Configuration
public class SwaggerConfig {
  @org.springframework.beans.factory.annotation.Value("${app.user-domain:ttodo.dev}")
  private String userDomain;

  private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

  @Bean
  @Profile("!prod")
  public OpenAPI defaultOpenAPI() {
    return createBaseOpenAPI();
  }

  @Bean
  @Profile("prod")
  public OpenAPI prodOpenAPI(@Value("${app.scheme}") String scheme,
                            @Value("${app.host}") String host,
                            @Value("${app.port}") int port) {
    var url = buildServerUrl(scheme, host, port);
    return createBaseOpenAPI()
            .servers(List.of(new Server().url(url).description("Production")));
  }

  private OpenAPI createBaseOpenAPI() {
    return new OpenAPI()
            .info(apiInfo())
            .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
            .components(securityComponents());
  }

  private Info apiInfo() {
    return new Info()
            .title("뚜두(TTODO) API")
            .version("1.0.0")
            .description("""
                    할 일 관리 애플리케이션 '뚜두(TTODO)' 백엔드 API 문서
                    
                    ## 뚜두(TTODO)란?
                    '뚜두'는 우표처럼 펀치가 있는 종이를 찢는 소리를 의성어로 표현한 것으로,
                    완료된 할 일을 찢어서 표시하는 컨셉의 할 일 관리 애플리케이션입니다.
                    
                    ## 주요 기능
                    - **할 일 관리**: 할 일 생성, 수정, 삭제, 완료 처리 및 반복 일정 설정
                    - **카테고리 관리**: 할 일을 체계적으로 분류할 수 있는 카테고리 기능
                    - **우선순위 설정**: 낮음, 보통, 높음 3단계 우선순위 설정
                    - **태그 시스템**: 할 일에 태그를 추가하여 더 세밀한 분류 가능
                    - **챌린지 기능**: 공개 챌린지 생성 및 참여로 목표 달성 동기 부여
                    - **통계 및 경험치**: 할 일 완료 통계와 레벨 시스템으로 성취감 제공
                    - **프로필 관리**: 사용자별 프로필 이미지, 테마, 타임존 설정
                    
                    ## 인증 방식
                    JWT 토큰 기반 인증을 사용합니다. 로그인 후 발급받은 액세스 토큰을
                    Authorization 헤더에 'Bearer {token}' 형식으로 포함하여 요청하세요.
                    """)
            .contact(new Contact()
                    .name("GET to the POINT Team")
                    .email("bunny@xiyo.dev"))
            .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0"));
  }

  private String buildServerUrl(String scheme, String host, int port) {
    if ((scheme.equals("http") && port == 80) || (scheme.equals("https") && port == 443)) {
      return "%s://%s".formatted(scheme, host);
    }
    return "%s://%s:%d".formatted(scheme, host, port);
  }

  private Components securityComponents() {
    return new Components()
            .addSecuritySchemes(SECURITY_SCHEME_NAME,
                    new SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                            .description("""
                                JWT 토큰 입력 (Bearer 접두사 제외)
                                
                                개발 환경에서는 다음 엔드포인트로 만료 없는 테스트 토큰을 발급받아 사용하세요:
                                - GET /auth/dev-token
                                
                                발급되는 토큰은 시스템 익명 사용자(anon@%s)의 권한으로 서명되며, 현재 서버 키로 서명되어 Swagger에서 바로 사용 가능합니다.
                                """.formatted(userDomain)));
  }
}
