package point.zzicback.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import point.zzicback.model.Todo;

@Data
@Schema(description = "To-Do 생성 요청"
        , requiredProperties = {"title"}
)
public class CreateTodoRequest {
    @Schema(description = "To-Do 항목의 제목", example = "장보기")
    @NotBlank
    private String title;

    @Schema(description = "To-Do 항목의 상세 설명", example = "우유, 빵, 계란 구입")
    private String description;

    public Todo ToEntity() {
        Todo todo = new Todo();
        todo.setTitle(title);
        todo.setDescription(description);
        todo.setDone(false);
        return todo;
    }
}

