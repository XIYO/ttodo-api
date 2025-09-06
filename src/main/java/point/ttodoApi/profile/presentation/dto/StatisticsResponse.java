package point.ttodoApi.profile.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * 통계 응답 DTO
 */
@Schema(description = "사용자 통계 응답")
public record StatisticsResponse(
        @Schema(description = "완료한 할일 수")
        Integer completedTodos,

        @Schema(description = "생성한 카테고리 수")
        Integer totalCategories
) {
}