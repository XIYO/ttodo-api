package point.zzicback.todo.application.dto.command;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateTodoCommand(@NotNull UUID memberId, @NotBlank @Size(max = 255) String title,
                                @Size(max = 1000) String description) {
}
