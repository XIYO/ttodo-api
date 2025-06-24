package point.zzicback.todo.application.dto.command;

import java.time.LocalDate;
import java.util.UUID;

public record CompleteVirtualTodoCommand(
        UUID memberId,
        Long originalTodoId,
        LocalDate completionDate
) {
}
