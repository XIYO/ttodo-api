package point.zzicback.todo.application.dto.command;

import jakarta.validation.constraints.*;

import java.util.UUID;

public record CreateTodoCommand(@NotNull UUID memberId, @NotBlank @Size(max = 255) String title,
                                @Size(max = 1000) String description) {
}
