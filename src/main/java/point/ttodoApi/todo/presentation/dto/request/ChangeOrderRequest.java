package point.ttodoApi.todo.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

public record ChangeOrderRequest(
        @Min(0)
        @Schema(description = "새로운 순서", example = "0")
        Integer newOrder
) {
}
