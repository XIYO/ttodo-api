package point.zzicback.todo.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public record ChangeOrderRequest(
    @NotNull
    @Min(0)
    @Schema(description = "새로운 순서", example = "0")
    Integer newOrder
) {}
