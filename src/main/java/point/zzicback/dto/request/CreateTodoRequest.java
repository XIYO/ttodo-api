package point.zzicback.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import point.zzicback.domain.Todo;

import jakarta.validation.constraints.NotBlank;

@Data
@Schema(description = "To-Do 생성 요청", requiredProperties = {"title"})
public class CreateTodoRequest {
    @NotBlank
    @Schema(description = "To-Do 항목의 제목", example = "장보기")
    private String title;

    @Schema(description = "To-Do 항목의 상세 설명", example = "우유, 빵, 계란 구입")
    private String description;

    /**
     * Request DTO → Entity 변환 메서드
     */
    public Todo toEntity() {
        Todo todo = new Todo();
        todo.setTitle(this.title);
        todo.setDescription(this.description);
        todo.setDone(false);
        return todo;
    }
}
