package point.zzicback.todo.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TodoStatus {
    IN_PROGRESS("진행중"),
    COMPLETED("완료"),
    OVERDUE("지연");

    private final String displayName;
}
