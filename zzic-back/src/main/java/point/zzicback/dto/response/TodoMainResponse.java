package point.zzicback.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "To-Do 응답 모델")
public class TodoMainResponse {
    @Schema(description = "To-Do 항목의 고유 식별자", example = "1", required = true)
    private Integer id;

    @Schema(description = "To-Do 항목의 제목", example = "장보기", required = true)
    private String title;

    @Schema(description = "To-Do 항목의 완료 여부", example = "false", required = true)
    private Boolean done;
}