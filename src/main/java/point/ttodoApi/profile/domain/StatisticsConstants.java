package point.ttodoApi.profile.domain;

/**
 * Statistics 도메인 제약사항 상수
 */
public final class StatisticsConstants {

    private StatisticsConstants() {
        // 인스턴스화 방지
    }

    // 통계 수치 제약사항
    public static final int MIN_COUNT = 0;
    public static final int MAX_COUNT = 2_100_000_000; // 약 20억
    public static final long MIN_FOCUS_TIME = 0L;
    public static final long MAX_FOCUS_TIME = 157_680_000L; // 5년 분(초)
    public static final int MIN_STREAK_DAYS = 0;
    public static final int MAX_STREAK_DAYS = 3650; // 10년
    
    // 기본값
    public static final int DEFAULT_COUNT = 0;
    public static final long DEFAULT_FOCUS_TIME = 0L;
    public static final int DEFAULT_STREAK_DAYS = 0;

    // 검증 메시지
    public static final String OWNER_REQUIRED_MESSAGE = "소유자는 필수입니다";
    public static final String SUCCEEDED_TODOS_COUNT_MESSAGE = "성공한 TODO 수는 " + MIN_COUNT + "부터 " + MAX_COUNT + "까지 가능합니다";
    public static final String SUCCEEDED_CHALLENGES_COUNT_MESSAGE = "성공한 도전 수는 " + MIN_COUNT + "부터 " + MAX_COUNT + "까지 가능합니다";
    public static final String FOCUS_TIME_MESSAGE = "총 집중 시간은 " + MIN_FOCUS_TIME + "부터 " + MAX_FOCUS_TIME + "초까지 가능합니다";
    public static final String CURRENT_STREAK_MESSAGE = "현재 스트릭 일수는 " + MIN_STREAK_DAYS + "부터 " + MAX_STREAK_DAYS + "일까지 가능합니다";
    public static final String LONGEST_STREAK_MESSAGE = "최대 스트릭 일수는 " + MIN_STREAK_DAYS + "부터 " + MAX_STREAK_DAYS + "일까지 가능합니다";
    public static final String CATEGORY_COUNT_MESSAGE = "카테고리 수는 " + MIN_COUNT + "부터 " + MAX_COUNT + "까지 가능합니다";
}
