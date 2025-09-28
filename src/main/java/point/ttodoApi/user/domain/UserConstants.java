package point.ttodoApi.user.domain;

/**
 * User 도메인 제약사항 상수
 */
public final class UserConstants {

    private UserConstants() {
        // 인스턴스화 방지
    }

    // 필드 길이 제약사항
    public static final int EMAIL_MAX_LENGTH = 320; // RFC 5321 표준
    public static final int PASSWORD_MIN_LENGTH = 8;
    public static final int PASSWORD_MAX_LENGTH = 128;

    // 검증 메시지
    public static final String EMAIL_REQUIRED_MESSAGE = "이메일은 필수입니다";
    public static final String EMAIL_FORMAT_MESSAGE = "올바른 이메일 형식이 아닙니다";
    public static final String PASSWORD_REQUIRED_MESSAGE = "비밀번호는 필수입니다";
    public static final String PASSWORD_LENGTH_MESSAGE = "비밀번호는 8자 이상 128자 이하여야 합니다";
    public static final String PASSWORD_PATTERN_MESSAGE = "비밀번호는 영문, 숫자, 특수문자를 포함해야 합니다";
}
