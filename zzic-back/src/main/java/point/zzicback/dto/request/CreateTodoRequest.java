package point.zzicback.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "To-Do 생성 요청"
        , requiredProperties = {"title"}
)
public class CreateTodoRequest {
    @Schema(description = "To-Do 항목의 제목", example = "장보기")
    private String title;

    @Schema(description = "To-Do 항목의 상세 설명", example = "우유, 빵, 계란 구입")
    private String description;
}