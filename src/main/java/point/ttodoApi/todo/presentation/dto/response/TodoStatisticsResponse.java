package point.ttodoApi.todo.presentation.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "Todo 통계 응답 DTO")
public record TodoStatisticsResponse(
        @Schema(description = "통계 데이터 목록")
        List<StatisticsItem> content
) {
  @Schema(description = "통계 항목")
  public record StatisticsItem(
          @Schema(description = "항목 이름", example = "진행중")
          String statisticsName,

          @Schema(description = "항목 값", example = "0")
          long statisticsValue
  ) {
  }
}
