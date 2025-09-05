package point.ttodoApi.category.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;
import point.ttodoApi.shared.dto.BaseSearchRequest;

import java.util.*;

/**
 * Category 검색 요청 DTO
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "카테고리 검색 조건")
public class CategorySearchRequest extends BaseSearchRequest {
    
    @Schema(description = "소유자 ID", hidden = true)
    private UUID memberId;
    
    @Schema(description = "제목 검색 키워드", example = "개인")
    @Size(max = 100, message = "제목 검색어는 100자를 초과할 수 없습니다")
    private String titleKeyword;
    
    @Schema(description = "색상 코드", example = "#FF5733")
    @Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "올바른 HEX 색상 코드를 입력하세요")
    private String colorCode;
    
    @Schema(description = "아이콘 키워드", example = "star")
    @Size(max = 50, message = "아이콘 검색어는 50자를 초과할 수 없습니다")
    private String iconKeyword;
    
    @Schema(description = "공유 타입 목록", example = "[\"PUBLIC\", \"PRIVATE\"]")
    @Size(max = 10, message = "공유 타입은 최대 10개까지 선택할 수 있습니다")
    private List<String> shareTypes;
    
    @Schema(description = "하위 카테고리 포함 여부", example = "false")
    private boolean includeSubCategories;
    
    @Schema(description = "활성 상태만 조회", example = "true")
    @Builder.Default
    private Boolean active = true;
    
    @Schema(description = "상위 카테고리 ID (하위 카테고리 조회 시)")
    private UUID parentCategoryId;
    
    @Override
    public String getDefaultSort() {
        return "orderIndex,asc";
    }
    
    @Override
    protected void validateBusinessRules() {
        // 하위 카테고리 포함 시 상위 카테고리 ID는 null이어야 함
        if (includeSubCategories && parentCategoryId != null) {
            throw new IllegalArgumentException("하위 카테고리 포함 조회와 특정 상위 카테고리 조회는 동시에 사용할 수 없습니다");
        }
    }
    
    /**
     * 제목 키워드가 있는지 확인
     */
    public boolean hasTitleKeyword() {
        return titleKeyword != null && !titleKeyword.trim().isEmpty();
    }
    
    /**
     * 색상 필터가 있는지 확인
     */
    public boolean hasColorFilter() {
        return colorCode != null && !colorCode.trim().isEmpty();
    }
    
    /**
     * 아이콘 필터가 있는지 확인
     */
    public boolean hasIconFilter() {
        return iconKeyword != null && !iconKeyword.trim().isEmpty();
    }
    
    /**
     * 공유 타입 필터가 있는지 확인
     */
    public boolean hasShareTypeFilter() {
        return shareTypes != null && !shareTypes.isEmpty();
    }
    
    /**
     * 상위 카테고리 필터가 있는지 확인
     */
    public boolean hasParentCategoryFilter() {
        return parentCategoryId != null;
    }
}