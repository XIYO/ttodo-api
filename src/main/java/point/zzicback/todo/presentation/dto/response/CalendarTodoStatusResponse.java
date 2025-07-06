package point.zzicback.todo.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "캘린더 날짜별 Todo 현황")
public record CalendarTodoStatusResponse(
    @Schema(description = "날짜", example = "2025-06-01")
    LocalDate date,
    
    @Schema(description = "Todo 존재 여부", example = "true")
    boolean hasTodo
) {}
