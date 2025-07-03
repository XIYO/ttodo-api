package point.zzicback.todo.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Todo 삭제 요청 DTO")
public record DeleteTodoRequest(
        @NotNull
        @Schema(
            description = "전체 삭제 여부 (true: 원본 포함 전체 삭제, false: 해당 날짜만 숨김)", 
            example = "false"
        )
        Boolean deleteAll
) {}
