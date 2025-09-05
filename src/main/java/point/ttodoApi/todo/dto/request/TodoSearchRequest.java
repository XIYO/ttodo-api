package point.ttodoApi.todo.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import point.ttodoApi.shared.dto.*;

import java.time.LocalDate;
import java.util.*;

/**
 * Todo 검색 요청 DTO
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Todo 검색 조건")
public class TodoSearchRequest extends BaseSearchRequest {
    
    @Schema(description = "회원 ID", hidden = true)
    private UUID memberId;
    
    @Schema(description = "완료 여부", example = "false")
    private Boolean complete;
    
    @Schema(description = "검색 키워드 (제목 검색)", example = "회의")
    @Size(max = 100, message = "검색 키워드는 100자를 초과할 수 없습니다")
    private String keyword;
    
    @Schema(description = "카테고리 ID 목록")
    @Size(max = 50, message = "카테고리는 최대 50개까지 선택할 수 있습니다")
    private List<UUID> categoryIds;
    
    @Schema(description = "우선순위 ID 목록", example = "[1, 2, 3]")
    @Size(max = 10, message = "우선순위는 최대 10개까지 선택할 수 있습니다")
    private List<Integer> priorityIds;
    
    @Schema(description = "시작 날짜", example = "2024-01-01")
    private LocalDate startDate;
    
    @Schema(description = "종료 날짜", example = "2024-12-31")
    private LocalDate endDate;
    
    @Schema(description = "긴급 Todo만 조회", example = "false")
    private boolean urgentOnly;
    
    @Override
    public String getDefaultSort() {
        return "createdAt,desc";
    }
    
    @Override
    protected void validateBusinessRules() {
        // 날짜 범위 검증
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("종료 날짜는 시작 날짜 이후여야 합니다");
        }
        
        // 긴급 Todo 조회 시 우선순위 자동 설정
        if (urgentOnly && (priorityIds == null || priorityIds.isEmpty())) {
            priorityIds = List.of(1); // 우선순위 1 = 긴급
        }
    }
    
    /**
     * 날짜 범위 객체 생성
     */
    public DateRangeRequest toDateRange() {
        return DateRangeRequest.builder()
                .startDate(startDate)
                .endDate(endDate)
                .build();
    }
    
    /**
     * 키워드가 있는지 확인
     */
    public boolean hasKeyword() {
        return keyword != null && !keyword.trim().isEmpty();
    }
    
    /**
     * 카테고리 필터가 있는지 확인
     */
    public boolean hasCategoryFilter() {
        return categoryIds != null && !categoryIds.isEmpty();
    }
    
    /**
     * 우선순위 필터가 있는지 확인
     */
    public boolean hasPriorityFilter() {
        return priorityIds != null && !priorityIds.isEmpty();
    }
    
    /**
     * 날짜 필터가 있는지 확인
     */
    public boolean hasDateFilter() {
        return startDate != null || endDate != null;
    }
}