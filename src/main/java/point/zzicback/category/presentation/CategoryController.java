package point.zzicback.category.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import point.zzicback.category.application.CategoryService;
import point.zzicback.category.application.command.*;
import point.zzicback.category.presentation.dto.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/members/{memberId}/categories")
@RequiredArgsConstructor
@Tag(name = "카테고리", description = "카테고리 관리 API")
public class CategoryController {
    private final CategoryService categoryService;
    
    @GetMapping
    @Operation(summary = "카테고리 목록 조회", description = "사용자의 모든 카테고리를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "카테고리 목록 조회 성공")
    public Page<CategoryResponse> getCategories(
            @Parameter(description = "회원 ID") @PathVariable UUID memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name,asc") String sort) {
        Pageable pageable = createPageable(page, size, sort);
        return categoryService.getCategories(memberId, pageable);
    }
    
    @GetMapping("/{categoryId}")
    @Operation(summary = "카테고리 상세 조회", description = "특정 카테고리의 상세 정보를 조회합니다.")
    @ApiResponse(responseCode = "200", description = "카테고리 조회 성공")
    public CategoryResponse getCategory(
            @Parameter(description = "회원 ID") @PathVariable UUID memberId,
            @Parameter(description = "카테고리 ID") @PathVariable Long categoryId) {
        return categoryService.getCategory(memberId, categoryId);
    }
    
    @PostMapping
    @Operation(summary = "카테고리 생성", description = "새로운 카테고리를 생성합니다.")
    @ApiResponse(responseCode = "201", description = "카테고리 생성 성공")
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponse createCategory(
            @Parameter(description = "회원 ID") @PathVariable UUID memberId,
            @Valid @RequestBody CreateCategoryRequest request) {
        CreateCategoryCommand command = new CreateCategoryCommand(
                memberId, request.name());
        return categoryService.createCategory(command);
    }
    
    @PutMapping("/{categoryId}")
    @Operation(summary = "카테고리 수정", description = "기존 카테고리를 수정합니다.")
    @ApiResponse(responseCode = "200", description = "카테고리 수정 성공")
    public CategoryResponse updateCategory(
            @Parameter(description = "회원 ID") @PathVariable UUID memberId,
            @Parameter(description = "카테고리 ID") @PathVariable Long categoryId,
            @Valid @RequestBody UpdateCategoryRequest request) {
        UpdateCategoryCommand command = new UpdateCategoryCommand(
                memberId, categoryId, request.name());
        return categoryService.updateCategory(command);
    }
    
    @DeleteMapping("/{categoryId}")
    @Operation(summary = "카테고리 삭제", description = "기존 카테고리를 삭제합니다.")
    @ApiResponse(responseCode = "204", description = "카테고리 삭제 성공")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(
            @Parameter(description = "회원 ID") @PathVariable UUID memberId,
            @Parameter(description = "카테고리 ID") @PathVariable Long categoryId) {
        DeleteCategoryCommand command = new DeleteCategoryCommand(memberId, categoryId);
        categoryService.deleteCategory(command);
    }
    
    private Pageable createPageable(int page, int size, String sort) {
        String[] sortParams = sort.split(",");
        String property = sortParams[0];
        String direction = sortParams.length > 1 ? sortParams[1] : "asc";
        return PageRequest.of(page, size, Sort.by(Sort.Direction.fromString(direction), property));
    }
}
