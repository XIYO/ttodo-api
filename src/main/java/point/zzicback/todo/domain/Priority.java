package point.zzicback.todo.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Priority {
    HIGH("높음"),
    MEDIUM("보통"),
    LOW("낮음");

    private final String displayName;
}
