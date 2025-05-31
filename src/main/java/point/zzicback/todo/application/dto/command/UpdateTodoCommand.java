package point.zzicback.todo.application.dto.command;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record UpdateTodoCommand(@NotNull UUID memberId, @NotNull Long todoId, @Size(max = 255) String title,
                                @Size(max = 1000) String description, Boolean done) {
}
