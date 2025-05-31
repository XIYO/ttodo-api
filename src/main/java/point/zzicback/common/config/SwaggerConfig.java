package point.zzicback.common.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("찌익 (ZZIC) 백엔드 API 문서")
                                .version("1.0.0")
                                .description(
                                        """
                TODO 기능의 백엔드 기능을 담당하는 API 문서 입니다.

                ## ZZIC 무엇인가요?

                우표처럼 펀치가 있는 종이를 찢는 소리를 의성어로 나타내었습니다.
                완료된 투두는 찢어 표시하는 것처럼 완료된 투두를 표시합니다.
                """)
                                .license(
                                        new License()
                                                .name("Apache 2.0")
                                                .url("https://springdoc.org")));
    }
}
