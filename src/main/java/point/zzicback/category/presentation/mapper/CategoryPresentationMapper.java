package point.zzicback.category.presentation.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import point.zzicback.category.application.dto.result.CategoryResult;
import point.zzicback.category.presentation.dto.response.CategoryResponse;

/**
 * Category Presentation Layer Mapper
 * CategoryResult(내부) -> CategoryResponse(외부) 변환
 * DDD 원칙에 따라 외부 레이어가 내부 객체를 알고 변환
 */
@Mapper(componentModel = "spring")
public interface CategoryPresentationMapper {
    
    /**
     * Application Layer의 Result를 Presentation Layer의 Response로 변환
     * 
     * @param result Application Layer의 결과 DTO
     * @return Presentation Layer의 응답 DTO
     */
    @Mapping(source = "colorHex", target = "color")
    CategoryResponse toResponse(CategoryResult result);
}