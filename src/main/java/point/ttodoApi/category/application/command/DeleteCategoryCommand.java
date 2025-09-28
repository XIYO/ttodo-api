package point.ttodoApi.category.application.command;

import point.ttodoApi.category.domain.validation.required.ValidCategoryId;
import point.ttodoApi.user.domain.validation.required.ValidUserId;

import java.util.UUID;

/**
 * 카테고리 삭제 커맨드
 * TTODO 아키텍처 패턴: Command 객체로 카테고리 삭제 요청 캡슐화
 */
public record DeleteCategoryCommand(
        @ValidUserId
        UUID userId,
        
        @ValidCategoryId
        UUID categoryId
) {
}
