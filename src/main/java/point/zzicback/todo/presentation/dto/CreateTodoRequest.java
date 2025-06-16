package point.zzicback.todo.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import point.zzicback.todo.domain.*;

import java.time.LocalDate;
import java.util.Set;

@Schema(description = "Todo 생성 요청 DTO")
public record CreateTodoRequest(
        @NotBlank @Size(max = 255) 
        @Schema(description = "할일 제목", example = "영어 공부하기", required = true)
        String title, 
        
        @Size(max = 1000) 
        @Schema(description = "할일 설명", example = "토익 문제집 2장 풀기")
        String description,
        
        @Schema(description = "상태", example = "IN_PROGRESS", defaultValue = "IN_PROGRESS")
        TodoStatus status,
        
        @Schema(description = "우선순위 (0: 낮음, 1: 보통, 2: 높음)", example = "1")
        Integer priority,
        
        @Schema(description = "카테고리 ID", example = "1")
        Long categoryId,

        @Schema(description = "마감일", example = "2026-01-01", type = "string", format = "date")
        LocalDate dueDate,
        
        @Schema(description = "반복 유형", example = "DAILY", defaultValue = "NONE")
        RepeatType repeatType,
        
        @Schema(description = "태그 목록 (최대 10개)", example = "[\"영어\", \"학습\"]", nullable = true)
        Set<String> tags
) {
}
