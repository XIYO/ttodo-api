package point.zzicback.category.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "카테고리 응답")
public record CategoryResponse(
        @Schema(description = "카테고리 ID", example = "1")
        Long id,

        @Schema(description = "카테고리명", example = "업무")
        String name,

        @Schema(description = "카테고리 색상", example = "#ff0000")
        String color,

        @Schema(description = "카테고리 설명", example = "회사 업무 관련")
        String description
) {
}
