package point.zzicback.common.config;

import io.swagger.v3.oas.models.*;
import io.swagger.v3.oas.models.info.*;
import io.swagger.v3.oas.models.security.*;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.*;

import java.util.List;

@Configuration
public class SwaggerConfig {

  private static final String SECURITY_SCHEME_NAME = "Bearer Authentication";

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI()
            .info(apiInfo())
            // .servers(serverList())
            .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
            .components(securityComponents());
  }

  private Info apiInfo() {
    return new Info()
            .title("찌익 (ZZIC) API")
            .version("1.0.0")
            .description("""
                    TODO 관리 애플리케이션 백엔드 API
                    
                    ## ZZIC이란?
                    우표처럼 펀치가 있는 종이를 찢는 소리를 의성어로 표현한 것으로,
                    완료된 TODO를 찢어서 표시하는 컨셉의 애플리케이션입니다.
                    """)
            .contact(new Contact()
                    .name("ZZIC Team")
                    .email("contact@zzic.point"))
            .license(new License()
                    .name("Apache 2.0")
                    .url("https://www.apache.org/licenses/LICENSE-2.0"));
  }

  private List<Server> serverList() {
    return List.of(
            new Server().url("http://localhost:8080").description("Local"),
            new Server().url("https://api.zzic.point").description("Production")
    );
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
