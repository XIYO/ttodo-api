package point.ttodoApi.challenge.presentation.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

@Schema(description = "챌린지 투두 생성 요청 DTO")
public record CreateTodoRequest(
    @NotBlank(message = "투두 제목은 필수입니다")
    @Size(min = 1, max = 255, message = "투두 제목은 1자 이상 255자 이하여야 합니다")
    @Schema(description = "투두 제목", example = "물 2L 마시기")
    String title,
    
    @Size(max = 1000, message = "투두 설명은 1000자 이하여야 합니다")
    @Schema(description = "투두 설명", example = "하루에 물 2리터를 마시는 것이 목표입니다")
    String description
) {}