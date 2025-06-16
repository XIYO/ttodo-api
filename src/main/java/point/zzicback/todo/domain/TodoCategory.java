package point.zzicback.todo.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TodoCategory {
    PERSONAL("개인"),
    WORK("업무"),
    HEALTH("건강"),
    LEARNING("학습"),
    SHOPPING("쇼핑"),
    FAMILY("가족"),
    OTHER("기타");

    private final String displayName;
}
