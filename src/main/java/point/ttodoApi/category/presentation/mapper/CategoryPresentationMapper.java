package point.ttodoApi.category.presentation.mapper;

import org.mapstruct.*;
import point.ttodoApi.category.application.result.CategoryResult;
import point.ttodoApi.category.domain.Category;
import point.ttodoApi.category.presentation.dto.response.CategoryResponse;

/**
 * Category Presentation Layer Mapper
 * CategoryResult(내부) -> CategoryResponse(외부) 변환
 * DDD 원칙에 따라 외부 레이어가 내부 객체를 알고 변환
 */
@Mapper(componentModel = "spring")
@SuppressWarnings("NullableProblems")
public interface CategoryPresentationMapper {

  /**
   * Application Layer의 Result를 Presentation Layer의 Response로 변환
   *
   * @param result Application Layer의 결과 DTO
   * @return Presentation Layer의 응답 DTO
   */
  @Mapping(source = "colorHex", target = "color")
  @Mapping(source = "displayOrder", target = "orderIndex")
  CategoryResponse toResponse(CategoryResult result);

  /**
   * Domain Entity를 Presentation Layer의 Response로 변환
   *
   * @param category Domain Entity
   * @return Presentation Layer의 응답 DTO
   */
  CategoryResponse toResponse(Category category);
}