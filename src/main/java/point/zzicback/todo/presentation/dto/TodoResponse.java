package point.zzicback.todo.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import point.zzicback.todo.domain.*;

import java.time.LocalDate;
import java.util.Set;

@Schema(description = "Todo 응답 DTO")
public record TodoResponse(
        @Schema(description = "Todo ID", example = "1")
        Long id, 
        
        @Schema(description = "할일 제목", example = "영어 공부하기")
        String title, 
        
        @Schema(description = "할일 설명", example = "토익 문제집 2장 풀기")
        String description, 
        
        @Schema(description = "상태", example = "IN_PROGRESS", allowableValues = {"IN_PROGRESS", "COMPLETED", "OVERDUE"})
        TodoStatus status,
        
        @Schema(description = "우선순위", example = "MEDIUM", allowableValues = {"HIGH", "MEDIUM", "LOW"})
        Priority priority,
        
        @Schema(description = "카테고리", example = "LEARNING", allowableValues = {"PERSONAL", "WORK", "HEALTH", "LEARNING", "SHOPPING", "FAMILY", "OTHER"})
        TodoCategory category,
        
        @Schema(description = "커스텀 카테고리명 (category가 OTHER일 때만 표시)", example = "내 프로젝트")
        String customCategory,
        
        @Schema(description = "마감일", example = "2024-12-31")
        LocalDate dueDate,
        
        @Schema(description = "반복 유형", example = "DAILY")
        RepeatType repeatType,
        
        @Schema(description = "태그 목록", example = "[\"영어\", \"학습\"]")
        Set<String> tags,
        
        @Schema(description = "표시용 카테고리명", example = "학습")
        String displayCategory,
        
        @Schema(description = "표시용 우선순위명", example = "보통")
        String displayPriority,
        
        @Schema(description = "표시용 상태명", example = "진행중")
        String displayStatus
) {
}
