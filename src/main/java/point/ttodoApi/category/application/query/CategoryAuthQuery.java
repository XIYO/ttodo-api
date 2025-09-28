package point.ttodoApi.category.application.query;

import point.ttodoApi.category.domain.validation.required.ValidCategoryId;

import java.util.UUID;

/**
 * 카테고리 권한 검증 쿼리
 * TTODO 아키텍처 패턴: Query 객체로 권한 검증용 카테고리 조회 요청 캡슐화
 */
public record CategoryAuthQuery(
        @ValidCategoryId
        UUID categoryId
) {
}