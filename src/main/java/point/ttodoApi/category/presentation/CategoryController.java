package point.ttodoApi.category.presentation;

import io.swagger.v3.oas.annotations.*;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import point.ttodoApi.category.application.*;
import point.ttodoApi.category.application.query.*;
import point.ttodoApi.category.application.result.CategoryResult;
import point.ttodoApi.category.application.command.*;
import point.ttodoApi.category.presentation.dto.request.*;
import point.ttodoApi.category.presentation.dto.response.*;
import point.ttodoApi.category.presentation.mapper.CategoryPresentationMapper;

import java.util.UUID;
import point.ttodoApi.shared.validation.*;



@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
@Tag(name = "할일 카테고리 관리", description = "할일을 분류하기 위한 카테고리 생성, 조회, 수정, 삭제 및 색상/이름 관리 API")
public class CategoryController {
  private final CategoryCommandService categoryCommandService; // TTODO: Command 처리
  private final CategoryQueryService categoryQueryService; // TTODO: Query 처리
  private final CategorySearchService categorySearchService;
  private final CategoryPresentationMapper mapper;

  @GetMapping
  @Operation(
          summary = "카테고리 목록 조회/검색",
          description = "사용자의 카테고리를 조회하거나 검색합니다. 다양한 필터를 통해 카테고리를 검색할 수 있습니다.\n\n" +
                  "검색 파라미터:\n" +
                  "- titleKeyword: 제목 키워드\n" +
                  "- colorCode: 색상 코드 (예: #FF5733)\n" +
                  "- iconKeyword: 아이콘 키워드\n" +
                  "- shareTypes: 공유 타입 목록\n" +
                  "- includeSubCategories: 하위 카테고리 포함 여부\n" +
                  "- parentCategoryId: 상위 카테고리 ID"
  )
  @ApiResponse(responseCode = "200", description = "카테고리 목록 조회 성공")
  @ValidPageable(sortFields = SortFieldsProvider.CATEGORY)
  public Page<CategoryResponse> getCategories(
          @AuthenticationPrincipal org.springframework.security.core.userdetails.User user,
          @Parameter(description = "검색 조건") @ModelAttribute CategorySearchRequest request,
          @Parameter(description = "페이징 및 정렬 정보")
          @PageableDefault(size = 20, sort = "orderIndex", direction = Sort.Direction.ASC) Pageable pageable) {

    request.setUserId(UUID.fromString(user.getUsername()));
    request.validate();

    // 검색 조건이 있으면 검색 서비스 사용, 없으면 기존 서비스 사용
    if (hasSearchCriteria(request)) {
      return categorySearchService.searchCategories(request, pageable)
              .map(mapper::toResponse);
    } else {
      // TTODO 아키텍처 패턴: MapStruct 매퍼를 통한 Query 생성
      var query = mapper.toCategoryPageQuery(UUID.fromString(user.getUsername()), pageable);
      return categoryQueryService.getCategories(query).map(mapper::toResponse);
    }
  }

  private boolean hasSearchCriteria(CategorySearchRequest request) {
    return request.hasTitleKeyword() ||
            request.hasColorFilter() ||
            request.hasIconFilter() ||
            request.hasShareTypeFilter() ||
            request.hasParentCategoryFilter() ||
            request.isIncludeSubCategories();
  }

  @GetMapping("/{categoryId}")
  @Operation(summary = "카테고리 상세 조회", description = "특정 카테고리의 상세 정보를 조회합니다.")
  @ApiResponse(responseCode = "200", description = "카테고리 조회 성공")
  @ApiResponse(responseCode = "403", description = "접근 권한 없음")
  @ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음")
  @PreAuthorize("@categoryQueryService.isUser(#categoryId, authentication.principal.id)")
  public CategoryResponse getCategory(
          @AuthenticationPrincipal org.springframework.security.core.userdetails.User user,
          @Parameter(description = "카테고리 ID") @PathVariable UUID categoryId) {
    // TTODO 아키텍처 패턴: MapStruct 매퍼를 통한 Query 생성
    var query = mapper.toCategoryQuery(categoryId, UUID.fromString(user.getUsername()));
    CategoryResult result = categoryQueryService.getCategory(query);
    return mapper.toResponse(result);
  }

  @PostMapping(consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
  @Operation(
          summary = "카테고리 생성",
          description = "새로운 카테고리를 생성합니다."
  )
  @ApiResponse(responseCode = "201", description = "카테고리 생성 성공")
  @ResponseStatus(HttpStatus.CREATED)
  public CategoryResponse createCategory(
          @AuthenticationPrincipal org.springframework.security.core.userdetails.User user,
          @Valid CreateCategoryRequest request) {
    // TTODO 아키텍처 패턴: MapStruct 매퍼를 통한 Command 생성
    var command = mapper.toCommand(request, UUID.fromString(user.getUsername()));
    CategoryResult result = categoryCommandService.createCategory(command);
    return mapper.toResponse(result);
  }

  @PutMapping(value = "/{categoryId}", consumes = {MediaType.APPLICATION_FORM_URLENCODED_VALUE, MediaType.MULTIPART_FORM_DATA_VALUE})
  @Operation(
          summary = "카테고리 수정",
          description = "기존 카테고리를 수정합니다."
  )
  @ApiResponse(responseCode = "200", description = "카테고리 수정 성공")
  @ApiResponse(responseCode = "403", description = "접근 권한 없음")
  @ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음")
  @PreAuthorize("@categoryQueryService.isUser(#categoryId, authentication.principal.id)")
  public CategoryResponse updateCategory(
          @AuthenticationPrincipal org.springframework.security.core.userdetails.User user,
          @Parameter(description = "카테고리 ID") @PathVariable UUID categoryId,
          @Valid UpdateCategoryRequest request) {
    // TTODO 아키텍처 패턴: MapStruct 매퍼를 통한 Command 생성
    var command = mapper.toCommand(request, UUID.fromString(user.getUsername()), categoryId);
    CategoryResult result = categoryCommandService.updateCategory(command);
    return mapper.toResponse(result);
  }

  @DeleteMapping("/{categoryId}")
  @Operation(summary = "카테고리 삭제", description = "기존 카테고리를 삭제합니다.")
  @ApiResponse(responseCode = "204", description = "카테고리 삭제 성공")
  @ApiResponse(responseCode = "403", description = "접근 권한 없음")
  @ApiResponse(responseCode = "404", description = "카테고리를 찾을 수 없음")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  @PreAuthorize("@categoryQueryService.isUser(#categoryId, authentication.principal.id)")
  public void deleteCategory(
          @AuthenticationPrincipal org.springframework.security.core.userdetails.User user,
          @Parameter(description = "카테고리 ID") @PathVariable UUID categoryId) {
    // TTODO 아키텍처 패턴: MapStruct 매퍼를 통한 Command 생성
    var command = mapper.toDeleteCommand(UUID.fromString(user.getUsername()), categoryId);
    categoryCommandService.deleteCategory(command);
  }
}
