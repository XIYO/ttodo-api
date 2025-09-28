package point.ttodoApi.category.application.query;

import point.ttodoApi.category.domain.validation.required.ValidCategoryId;
import point.ttodoApi.user.domain.validation.required.ValidUserId;

import java.util.UUID;

/**
 * 카테고리 조회 쿼리
 * TTODO 아키텍처 패턴: Query 객체로 카테고리 조회 요청 캡슐화
 */
public record CategoryQuery(
        @ValidCategoryId
        UUID categoryId,
        
        @ValidUserId
        UUID userId
) {
}