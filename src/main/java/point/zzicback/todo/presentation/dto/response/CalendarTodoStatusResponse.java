package point.zzicback.todo.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "캘린더 Todo 상태 응답")
public record CalendarTodoStatusResponse(
        @Schema(description = "날짜", example = "2025-06-15")
        LocalDate date,
        
        @Schema(description = "해당 날짜에 Todo 존재 여부", example = "true")
        boolean hasTodo
) {
}
