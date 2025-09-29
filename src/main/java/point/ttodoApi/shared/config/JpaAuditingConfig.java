package point.ttodoApi.shared.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing 설정
 * @WebMvcTest에서 제외하기 위해 Application 클래스에서 분리
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}