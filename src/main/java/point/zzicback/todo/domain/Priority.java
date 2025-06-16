package point.zzicback.todo.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Priority {
    _0("낮음", 0),
    _1("보통", 1),
    _2("높음", 2);

    private final String displayName;
    private final int value;
}
