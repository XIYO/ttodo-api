package point.ttodoApi.experience.domain;

/**
 * Experience 도메인 제약사항 상수
 */
public final class ExperienceConstants {

    private ExperienceConstants() {
        // 인스턴스화 방지
    }

    // 경험치 제약사항
    public static final int MIN_EXPERIENCE = 0;
    public static final int MAX_EXPERIENCE = 2_100_000_000; // 약 20억
    public static final int EXPERIENCE_INCREMENT_MIN = 1;
    public static final int EXPERIENCE_INCREMENT_MAX = 10_000;

    // 레벨 제약사항 (Level 엔티티 기반)
    public static final int MIN_LEVEL = 1;
    public static final int MAX_LEVEL = 100; // 확장 가능
    public static final int DEFAULT_LEVEL = 1;
    
    // 기본 경험치 지급량
    public static final int TODO_COMPLETION_XP = 10;
    public static final int CHALLENGE_COMPLETION_XP = 50;
    public static final int DAILY_LOGIN_XP = 5;

    // 검증 메시지
    public static final String OWNER_ID_REQUIRED_MESSAGE = "소유자 ID는 필수입니다";
    public static final String EXPERIENCE_RANGE_MESSAGE = "경험치는 " + MIN_EXPERIENCE + "부터 " + MAX_EXPERIENCE + "까지 가능합니다";
    public static final String EXPERIENCE_INCREMENT_MESSAGE = "경험치 증감량은 " + EXPERIENCE_INCREMENT_MIN + "부터 " + EXPERIENCE_INCREMENT_MAX + "까지 가능합니다";
    public static final String LEVEL_RANGE_MESSAGE = "레벨은 " + MIN_LEVEL + "부터 " + MAX_LEVEL + "까지 가능합니다";
}
