package point.ttodoApi.shared.config.shared;

import org.springframework.context.annotation.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.config.PageableHandlerMethodArgumentResolverCustomizer;

/**
 * Pageable 관련 전역 설정
 */
@Configuration
public class PageableConfig {

  /**
   * Pageable 기본값 설정
   * - 기본 페이지 크기: 20
   * - 최대 페이지 크기: 100
   * - 기본 정렬: id,desc
   */
  @Bean
  public PageableHandlerMethodArgumentResolverCustomizer pageableCustomizer() {
    return resolver -> {
      // 기본 페이지 크기 설정
      resolver.setFallbackPageable(PageRequest.of(0, 20));

      // 최대 페이지 크기 제한
      resolver.setMaxPageSize(100);

      // 페이지 파라미터 이름 설정 (기본값 유지)
      resolver.setPageParameterName("page");
      resolver.setSizeParameterName("size");

      // 1-based 페이지 번호 사용 여부 (false = 0-based)
      resolver.setOneIndexedParameters(false);
    };
  }
}