package point.ttodoApi.category.domain;

/**
 * Category 도메인 제약사항 상수
 */
public final class CategoryConstants {

    private CategoryConstants() {
        // 인스턴스화 방지
    }

    // 필드 길이 제약사항
    public static final int NAME_MAX_LENGTH = 100;
    public static final int NAME_MIN_LENGTH = 1;
    public static final int COLOR_LENGTH = 7; // #RRGGBB 형식
    public static final int DESCRIPTION_MAX_LENGTH = 255;
    public static final int INVITATION_MESSAGE_MAX_LENGTH = 500;

    // 기본값
    public static final String DEFAULT_COLOR = "#0078D4"; // 파란색

    // 검증 메시지
    public static final String NAME_REQUIRED_MESSAGE = "카테고리 이름은 필수입니다";
    public static final String NAME_SIZE_MESSAGE = "카테고리 이름은 1자 이상 100자 이하여야 합니다";
    public static final String COLOR_FORMAT_MESSAGE = "색상은 #RRGGBB 형식이어야 합니다";
    public static final String DESCRIPTION_SIZE_MESSAGE = "설명은 255자 이하여야 합니다";
    public static final String INVITATION_MESSAGE_SIZE_MESSAGE = "초대 메시지는 500자 이하여야 합니다";
    public static final String OWNER_REQUIRED_MESSAGE = "카테고리 소유자는 필수입니다";
    public static final String CATEGORY_REQUIRED_MESSAGE = "카테고리는 필수입니다";
    public static final String USER_REQUIRED_MESSAGE = "사용자는 필수입니다";
    public static final String STATUS_REQUIRED_MESSAGE = "협업자 상태는 필수입니다";
    public static final String INVITED_AT_REQUIRED_MESSAGE = "초대 날짜는 필수입니다";

    // 색상 검증 정규표현식
    public static final String HEX_COLOR_PATTERN = "^#[0-9A-Fa-f]{6}$";
}
