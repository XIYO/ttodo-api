package point.ttodoApi.todo.application.result;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Todo 통계 정보")
public record TodoStatistics(
        @Schema(description = "전체 개수", example = "6")
        long total,

        @Schema(description = "진행중 개수", example = "4")
        long inProgress,

        @Schema(description = "완료 개수", example = "2")
        long completed
) {
}
