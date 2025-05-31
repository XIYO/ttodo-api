package point.zzicback.todo.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateTodoRequest(
        @NotBlank @Size(max = 255) String title, @Size(max = 1000) String description) {}
