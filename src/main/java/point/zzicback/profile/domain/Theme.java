package point.zzicback.profile.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Theme {
    LIGHT("light"),
    DARK("dark"),
    AUTO("auto");
    
    private final String value;
}