package point.ttodoApi.category.application.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import point.ttodoApi.category.application.command.CreateCategoryCommand;
import point.ttodoApi.category.application.result.CategoryResult;
import point.ttodoApi.category.domain.Category;

/**
 * Category Application Mapper
 * TTODO 아키텍처 패턴: Application Layer 매퍼
 * Domain ↔ Application DTO 변환
 */
@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
@SuppressWarnings("NullableProblems")
public interface CategoryApplicationMapper {

    /**
     * CreateCategoryCommand → Category 엔티티 변환
     */
    @Mapping(target = "owner", ignore = true) // 서비스에서 설정
    Category toEntity(CreateCategoryCommand command);

    /**
     * Category 엔티티 → CategoryResult 변환
     */
    @Mapping(target = "colorHex", source = "color")
    @Mapping(target = "displayOrder", source = "orderIndex")
    CategoryResult toResult(Category category);
    
    /**
     * Category 엔티티 → CategoryResult 변환 (커스텀 매핑)
     */
    default CategoryResult toCategoryResult(Category category) {
        return new CategoryResult(
            category.getId(),
            category.getName(),
            category.getColor(),
            category.getDescription(),
            category.getOrderIndex(),
            category.getCreatedAt(),
            category.getUpdatedAt()
        );
    }
}