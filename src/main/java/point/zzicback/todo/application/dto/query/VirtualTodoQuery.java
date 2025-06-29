package point.zzicback.todo.application.dto.query;

import java.util.UUID;

public record VirtualTodoQuery(UUID memberId, Long originalTodoId, Long daysDifference) {
    public static VirtualTodoQuery of(UUID memberId, Long originalTodoId, Long daysDifference) {
        return new VirtualTodoQuery(memberId, originalTodoId, daysDifference);
    }
}
