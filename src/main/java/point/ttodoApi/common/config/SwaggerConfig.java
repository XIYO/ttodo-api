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
            .title("TTODO API")
            .version("1.0.0")
            .description("""
                    TODO 관리 애플리케이션 백엔드 API
                    
                    ## TTODO이란?
                    우표처럼 펀치가 있는 종이를 찢는 소리를 의성어로 표현한 것으로,
                    완료된 TODO를 찢어서 표시하는 컨셉의 애플리케이션입니다.
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
                            .description("JWT 토큰 입력 (Bearer 접두사 제외)"));
  }
}
