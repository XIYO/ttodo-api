package point.ttodoApi.category.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "카테고리 응답")
public record CategoryResponse(
        @Schema(description = "카테고리 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID id,

        @Schema(description = "카테고리명", example = "업무")
        String name,

        @Schema(description = "카테고리 색상", example = "#ff0000")
        String color,

        @Schema(description = "카테고리 설명", example = "회사 업무 관련")
        String description
) {
}
