package point.ttodoApi.category.application.query;

import org.springframework.data.domain.Pageable;
import point.ttodoApi.user.domain.validation.required.ValidUserId;

import java.util.UUID;

/**
 * 카테고리 페이지 조회 쿼리
 * TTODO 아키텍처 패턴: Query 객체로 카테고리 페이징 조회 요청 캡슐화
 */
public record CategoryPageQuery(
        @ValidUserId
        UUID userId,
        
        Pageable pageable
) {
}