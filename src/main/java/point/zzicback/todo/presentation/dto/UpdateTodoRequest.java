package point.zzicback.todo.presentation.dto;

import jakarta.validation.constraints.Size;

public record UpdateTodoRequest(
        @Size(max = 255) String title,
        @Size(max = 1000) String description,
        Boolean done
) {
}
