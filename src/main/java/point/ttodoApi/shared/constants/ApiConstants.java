package point.ttodoApi.shared.constants;

/**
 * API 관련 상수 정의
 */
public final class ApiConstants {

  private ApiConstants() {
    // 인스턴스화 방지
  }

  /**
   * HTTP 응답 코드
   */
  public static final class HttpStatus {
    public static final int CREATED = 201;
    public static final int NO_CONTENT = 204;
    public static final int BAD_REQUEST = 400;
    public static final int UNAUTHORIZED = 401;
    public static final int FORBIDDEN = 403;
    public static final int NOT_FOUND = 404;
    public static final int CONFLICT = 409;
    public static final int INTERNAL_SERVER_ERROR = 500;

    private HttpStatus() {
      // 인스턴스화 방지
    }
  }

  /**
   * 페이징 관련 상수
   */
  public static final class Pagination {
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    public static final int DEFAULT_SEARCH_SIZE = 30;

    private Pagination() {
      // 인스턴스화 방지
    }
  }

  /**
   * 경험치 관련 상수
   */
  public static final class Experience {
    public static final int TODO_COMPLETE_XP = 10;
    public static final int CHALLENGE_COMPLETE_XP = 20;

    private Experience() {
      // 인스턴스화 방지
    }
  }

  /**
   * 문자열 길이 제한
   */
  public static final class StringLimits {
    public static final int CATEGORY_NAME_MAX_LENGTH = 50;
    public static final int COLLABORATOR_MESSAGE_MAX_LENGTH = 255;
    public static final int LINE_MAX_LENGTH = 120;

    private StringLimits() {
      // 인스턴스화 방지
    }
  }

  /**
   * JWT 관련 상수
   */
  public static final class JWT {
    public static final int RSA_KEY_SIZE = 2048;
    public static final int TOKEN_EXPIRY_SECONDS = 3600;
    public static final int REFRESH_TOKEN_EXPIRY_SECONDS = 86400;
    public static final long DEV_TOKEN_EXPIRY_DAYS = 36500L; // 100년
    public static final int CLEANUP_BATCH_SIZE = 1000;
    public static final int RETRY_ATTEMPTS = 2;
    public static final int BACKOFF_MULTIPLIER = 5;

    private JWT() {
      // 인스턴스화 방지
    }
  }

  /**
   * 시간 관련 상수
   */
  public static final class TimeConstants {
    public static final int SECONDS_PER_HOUR = 3600;
    public static final int MILLISECONDS_PER_SECOND = 1000;
    public static final int HOURS_IN_DAY = 24;
    public static final int MINUTES_IN_HOUR = 60;
    public static final int END_OF_DAY_HOUR = 23;
    public static final int END_OF_DAY_MINUTE = 59;
    public static final int END_OF_DAY_SECOND = 59;

    private TimeConstants() {
      // 인스턴스화 방지
    }
  }
}