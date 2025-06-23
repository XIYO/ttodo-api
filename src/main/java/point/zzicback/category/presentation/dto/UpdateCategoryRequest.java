package point.zzicback.category.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "카테고리 수정 요청")
public record UpdateCategoryRequest(
        @NotBlank(message = "카테고리명은 필수입니다")
        @Size(max = 50, message = "카테고리명은 50자 이하로 입력해주세요")
        @Schema(description = "카테고리명", example = "업무")
        String name
) {
}
