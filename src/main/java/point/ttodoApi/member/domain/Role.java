package point.ttodoApi.member.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 회원 권한 Enum
 */
@Getter
@RequiredArgsConstructor
public enum Role {
    USER("ROLE_USER", "일반 사용자"),
    ADMIN("ROLE_ADMIN", "관리자"),
    GUEST("ROLE_GUEST", "게스트");
    
    private final String key;
    private final String description;
}