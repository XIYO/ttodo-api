package point.ttodoApi.challenge.domain;

/**
 * Challenge 도메인 제약사항 상수
 */
public final class ChallengeConstants {

    private ChallengeConstants() {
        // 인스턴스화 방지
    }

    // Challenge 필드 길이 제약사항
    public static final int TITLE_MIN_LENGTH = 1;
    public static final int TITLE_MAX_LENGTH = 100;
    public static final int DESCRIPTION_MAX_LENGTH = 5000;
    public static final int INVITE_CODE_LENGTH = 8;
    public static final int MAX_PARTICIPANTS_MIN = 1;
    public static final int MAX_PARTICIPANTS_MAX = 10000;
    
    // ChallengeLeader 필드 길이 제약사항
    public static final int REMOVAL_REASON_MAX_LENGTH = 500;
    
    // 기본값
    public static final ChallengeVisibility DEFAULT_VISIBILITY = ChallengeVisibility.PUBLIC;
    public static final Boolean DEFAULT_ACTIVE = true;
    public static final LeaderStatus DEFAULT_LEADER_STATUS = LeaderStatus.ACTIVE;
    
    // 비즈니스 규칙
    public static final double LEADER_MAX_PERCENTAGE = 30.0; // 리더는 참여자의 30%까지
    public static final int LEADER_MIN_COUNT = 1;
    public static final int LEADER_MAX_COUNT = 10;
    
    // 검증 메시지
    public static final String TITLE_REQUIRED_MESSAGE = "챌린지 제목은 필수입니다";
    public static final String START_DATE_REQUIRED_MESSAGE = "시작일은 필수입니다";
    public static final String END_DATE_REQUIRED_MESSAGE = "종료일은 필수입니다";
    public static final String PERIOD_TYPE_REQUIRED_MESSAGE = "기간 타입은 필수입니다";
    public static final String VISIBILITY_REQUIRED_MESSAGE = "공개 설정은 필수입니다";
    public static final String CREATOR_ID_REQUIRED_MESSAGE = "생성자 ID는 필수입니다";
    public static final String ACTIVE_REQUIRED_MESSAGE = "활성 상태는 필수입니다";
    public static final String USER_REQUIRED_MESSAGE = "사용자는 필수입니다";
    public static final String CHALLENGE_REQUIRED_MESSAGE = "챌린지는 필수입니다";
    public static final String APPOINTED_BY_REQUIRED_MESSAGE = "임명자는 필수입니다";
    public static final String STATUS_REQUIRED_MESSAGE = "상태는 필수입니다";
    public static final String DONE_REQUIRED_MESSAGE = "완료 상태는 필수입니다";
    public static final String TARGET_DATE_REQUIRED_MESSAGE = "목표일은 필수입니다";
    public static final String CHALLENGE_PARTICIPATION_REQUIRED_MESSAGE = "챌린지 참여는 필수입니다";
}