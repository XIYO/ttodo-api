package point.ttodoApi.category.presentation.mapper;

import org.mapstruct.*;
import point.ttodoApi.category.application.command.CreateCategoryCommand;
import point.ttodoApi.category.application.command.DeleteCategoryCommand;
import point.ttodoApi.category.application.command.UpdateCategoryCommand;
import point.ttodoApi.category.application.query.CategoryPageQuery;
import point.ttodoApi.category.application.query.CategoryQuery;
import point.ttodoApi.category.application.result.CategoryResult;
import point.ttodoApi.category.domain.Category;
import point.ttodoApi.category.presentation.dto.request.CreateCategoryRequest;
import point.ttodoApi.category.presentation.dto.request.UpdateCategoryRequest;
import point.ttodoApi.category.presentation.dto.response.CategoryResponse;
import point.ttodoApi.shared.config.MapStructConfig;

import java.util.UUID;
import org.springframework.data.domain.Pageable;

/**
 * Category Presentation Layer Mapper
 * CategoryResult(내부) -> CategoryResponse(외부) 변환
 * Request DTOs -> Command/Query 객체 변환
 * DDD 원칙에 따라 외부 레이어가 내부 객체를 알고 변환
 */
@Mapper(config = MapStructConfig.class)
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

  // ============= Command/Query 변환 메서드들 =============

  /**
   * CreateCategoryRequest → CreateCategoryCommand 변환
   */
  CreateCategoryCommand toCommand(CreateCategoryRequest request, UUID userId);

  /**
   * UpdateCategoryRequest → UpdateCategoryCommand 변환
   */
  UpdateCategoryCommand toCommand(UpdateCategoryRequest request, UUID userId, UUID categoryId);

  /**
   * Delete 파라미터 → DeleteCategoryCommand 변환
   */
  DeleteCategoryCommand toDeleteCommand(UUID userId, UUID categoryId);

  /**
   * CategoryQuery 생성
   */
  CategoryQuery toCategoryQuery(UUID categoryId, UUID userId);

  /**
   * CategoryPageQuery 생성
   */
  CategoryPageQuery toCategoryPageQuery(UUID userId, Pageable pageable);
}