package point.zzicback.category.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "카테고리 생성 요청")
public record CreateCategoryRequest(
        @NotBlank(message = "카테고리명은 필수입니다")
        @Size(max = 50, message = "카테고리명은 50자 이하로 입력해주세요")
        @Schema(description = "카테고리명", example = "업무")
        String name,

        @Schema(description = "카테고리 색상", example = "#ff0000")
        String color,

        @Size(max = 255, message = "카테고리 설명은 255자 이하로 입력해주세요")
        @Schema(description = "카테고리 설명", example = "회사 업무 관련")
        String description
) {
}
