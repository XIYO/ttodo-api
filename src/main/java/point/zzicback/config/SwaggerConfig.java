package point.zzicback.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class SwaggerConfig {

    private static final String PROD_SERVER_URL = "https://api.zzic.xiyo.dev"; // prod 환경의 추가 서버 URL

    @Bean
    public OpenAPI customOpenAPI(Environment environment) {
        OpenAPI openAPI = new OpenAPI()
                .info(new Info()
                        .title("ZZIC-API")
                        .description("""
                                ## ZZIC 무엇인가요?
                                
                                우표처럼 펀치가 있는 종이를 찢는 소리를 의성어로 나타내었습니다.
                                완료된 투두는 찢어 표시하는 것처럼 완료된 투두를 표시합니다.
                                """)
                        .license(new License().name("Apache 2.0").url("https://springdoc.org")));

        if (environment.matchesProfiles("prod")) {
            openAPI.addServersItem(new Server().url(PROD_SERVER_URL));
        }

        return openAPI;
    }
}