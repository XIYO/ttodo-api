package point.ttodoApi.profile.domain;

/**
 * Profile 도메인 제약사항 상수
 */
public final class ProfileConstants {

    private ProfileConstants() {
        // 인스턴스화 방지
    }

    // 필드 길이 제약사항
    public static final int NICKNAME_MAX_LENGTH = 100;
    public static final int INTRODUCTION_MAX_LENGTH = 500;
    public static final int IMAGE_URL_MAX_LENGTH = 500;
    public static final int TIME_ZONE_MAX_LENGTH = 50;
    public static final int LOCALE_MAX_LENGTH = 10;
    public static final int IMAGE_TYPE_MAX_LENGTH = 50;

    // 기본값
    public static final Theme DEFAULT_THEME = Theme.PINKY;
    public static final String DEFAULT_TIME_ZONE = "Asia/Seoul";
    public static final String DEFAULT_LOCALE = "ko-KR";

    // 검증 메시지
    public static final String OWNER_ID_REQUIRED_MESSAGE = "Owner ID는 필수입니다";
    public static final String NICKNAME_REQUIRED_MESSAGE = "닉네임은 필수입니다";
    public static final String THEME_REQUIRED_MESSAGE = "테마는 필수입니다";
}