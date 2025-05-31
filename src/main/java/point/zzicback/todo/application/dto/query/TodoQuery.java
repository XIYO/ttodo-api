package point.zzicback.todo.application.dto.query;

import java.util.UUID;

public record TodoQuery(UUID memberId, Long todoId) {
    public static TodoQuery of(UUID memberId, Long todoId) {
        return new TodoQuery(memberId, todoId);
    }
}
