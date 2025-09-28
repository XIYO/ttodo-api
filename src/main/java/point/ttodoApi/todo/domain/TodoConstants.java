package point.ttodoApi.todo.domain;

/**
 * Todo 도메인 제약사항 상수
 */
public final class TodoConstants {

    private TodoConstants() {
        // 인스턴스화 방지
    }

    // 필드 길이 제약사항
    public static final int TITLE_MAX_LENGTH = 255;
    public static final int DESCRIPTION_MAX_LENGTH = 1000;
    public static final int TAG_MAX_LENGTH = 50;
    public static final int MAX_TAGS_COUNT = 20;
    
    // 우선순위 제약사항
    public static final int PRIORITY_MIN_VALUE = 0;
    public static final int PRIORITY_MAX_VALUE = 2;
    
    // 순서 제약사항
    public static final int DISPLAY_ORDER_MIN_VALUE = 0;
    public static final int DISPLAY_ORDER_MAX_VALUE = 999999;
    
    // 기본값
    public static final boolean DEFAULT_COMPLETE = false;
    public static final boolean DEFAULT_IS_PINNED = false;
    public static final boolean DEFAULT_ACTIVE = true;
    public static final boolean DEFAULT_IS_COLLABORATIVE = false;
    public static final int DEFAULT_DISPLAY_ORDER = 0;
    
    // 검증 메시지
    public static final String TITLE_REQUIRED_MESSAGE = "제목은 필수 입력값입니다";
    public static final String TITLE_SIZE_MESSAGE = "제목은 1자 이상 " + TITLE_MAX_LENGTH + "자 이하여야 합니다";
    public static final String DESCRIPTION_SIZE_MESSAGE = "설명은 " + DESCRIPTION_MAX_LENGTH + "자 이하여야 합니다";
    public static final String PRIORITY_RANGE_MESSAGE = "우선순위는 " + PRIORITY_MIN_VALUE + "부터 " + PRIORITY_MAX_VALUE + "까지 입력 가능합니다";
    public static final String DISPLAY_ORDER_RANGE_MESSAGE = "순서는 " + DISPLAY_ORDER_MIN_VALUE + "부터 " + DISPLAY_ORDER_MAX_VALUE + "까지 입력 가능합니다";
    public static final String TAG_SIZE_MESSAGE = "태그는 " + TAG_MAX_LENGTH + "자 이하여야 합니다";
    public static final String TAG_COUNT_MESSAGE = "태그는 최대 " + MAX_TAGS_COUNT + "개까지 추가할 수 있습니다";
    public static final String DATE_FUTURE_MESSAGE = "날짜는 과거 날짜로 설정할 수 없습니다";
    public static final String OWNER_REQUIRED_MESSAGE = "소유자는 필수입니다";
}
