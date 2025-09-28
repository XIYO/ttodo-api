package point.ttodoApi.todo.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Todo 삭제 요청 DTO")
public record DeleteTodoRequest(
        @Schema(
                description = "전체 삭제 여부 (true: 원본 포함 전체 삭제, false: 해당 날짜만 숨김)",
                example = "false"
        )
        Boolean deleteAll
) {
}
