package point.zzicback.todo.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import point.zzicback.todo.domain.RepeatType;

import java.time.Instant;
import java.util.Set;

@Schema(description = "Todo 응답 DTO")
public record TodoResponse(
        @Schema(description = "Todo ID", example = "1")
        Long id, 
        
        @Schema(description = "할일 제목", example = "영어 공부하기")
        String title, 
        
        @Schema(description = "할일 설명", example = "토익 문제집 2장 풀기")
        String description, 
        
        @Schema(description = "상태 (0: 진행중, 1: 완료, 2: 지연)", example = "0", allowableValues = {"0", "1", "2"})
        Integer statusId,
        
        @Schema(description = "상태명", example = "진행중")
        String statusName,
        
        @Schema(description = "우선순위 (0: 낮음, 1: 보통, 2: 높음)", example = "1")
        Integer priorityId,
        
        @Schema(description = "우선순위명", example = "보통")
        String priorityName,
        
        @Schema(description = "카테고리 ID", example = "1")
        Long categoryId,
        
        @Schema(description = "카테고리명", example = "학습")
        String categoryName,
        
        @Schema(description = "마감 시각", example = "2024-12-31T23:59:59Z")
        Instant dueDate,
        
        @Schema(description = "반복 유형", example = "DAILY")
        RepeatType repeatType,
        
        @Schema(description = "태그 목록", example = "[\"영어\", \"학습\"]")
        Set<String> tags
) {
}
