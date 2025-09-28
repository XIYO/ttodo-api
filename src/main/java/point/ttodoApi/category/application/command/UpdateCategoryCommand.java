package point.ttodoApi.category.application.command;

import point.ttodoApi.category.domain.validation.required.ValidCategoryId;
import point.ttodoApi.category.domain.validation.required.ValidCategoryName;
import point.ttodoApi.category.domain.validation.optional.OptionalCategoryColor;
import point.ttodoApi.category.domain.validation.optional.OptionalCategoryDescription;
import point.ttodoApi.user.domain.validation.required.ValidUserId;

import java.util.UUID;

/**
 * 카테고리 수정 커맨드
 * TTODO 아키텍처 패턴: Command 객체로 카테고리 수정 요청 캡슐화
 */
public record UpdateCategoryCommand(
        @ValidUserId
        UUID userId,
        
        @ValidCategoryId
        UUID categoryId,
        
        @ValidCategoryName
        String name,
        
        @OptionalCategoryColor
        String color,
        
        @OptionalCategoryDescription
        String description,
        
        Integer orderIndex
) {
}
