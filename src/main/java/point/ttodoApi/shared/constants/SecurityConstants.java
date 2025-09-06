package point.ttodoApi.shared.constants;

/**
 * 보안 관련 상수 정의
 */
public final class SecurityConstants {

  private SecurityConstants() {
    // 인스턴스화 방지
  }

  /**
   * 역할(Role) 관련 상수
   */
  public static final class Roles {
    public static final String ROLE_USER = "ROLE_USER";
    public static final String ROLE_ADMIN = "ROLE_ADMIN";
    public static final String ROLE_SUPER_ADMIN = "ROLE_SUPER_ADMIN";

    // Spring Security 권한 체크용 (ROLE_ 프리픽스 제거)
    public static final String USER = "USER";
    public static final String ADMIN = "ADMIN";
    public static final String SUPER_ADMIN = "SUPER_ADMIN";

    private Roles() {
      // 인스턴스화 방지
    }
  }

  /**
   * 쿠키 관련 상수
   */
  public static final class Cookies {
    public static final String ACCESS_TOKEN_COOKIE = "access-token";
    public static final String REFRESH_TOKEN_COOKIE = "refresh-token";
    public static final String SAME_SITE_STRICT = "Strict";
    public static final String SAME_SITE_LAX = "Lax";

    private Cookies() {
      // 인스턴스화 방지
    }
  }
}