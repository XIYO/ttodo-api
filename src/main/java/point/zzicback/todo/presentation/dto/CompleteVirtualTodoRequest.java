package point.zzicback.todo.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

@Schema(description = "가상 투두 완료 요청")
public record CompleteVirtualTodoRequest(
        @Schema(description = "완료 날짜", example = "2025-06-01")
        @NotNull LocalDate completionDate
) {
}
