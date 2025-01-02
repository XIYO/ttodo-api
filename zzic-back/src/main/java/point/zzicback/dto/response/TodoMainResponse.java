package point.zzicback.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import point.zzicback.model.Todo;

@Data
@Schema(description = "To-Do 응답 모델", requiredProperties = {"id", "title", "done"})
public class TodoMainResponse {
    @Schema(description = "To-Do 항목의 고유 식별자", example = "1")
    private Long id;

    @Schema(description = "To-Do 항목의 제목", example = "장보기")
    private String title;

    @Schema(description = "To-Do 항목의 완료 여부", example = "false")
    private Boolean done;

    /**
     * Entity → Response DTO 변환 메서드
     */
    public static TodoMainResponse fromEntity(Todo todo) {
        TodoMainResponse response = new TodoMainResponse();
        response.setId(todo.getId());
        response.setTitle(todo.getTitle());
        response.setDone(todo.getDone());
        return response;
    }
}
