package point.ttodoApi.test.security;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Test annotation for mocking authenticated user principal
 *
 * Usage:
 * @WithMockUserPrincipal(userId = "test-user-id", email = "test@example.com", roles = {"ROLE_USER"})
 */
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockUserPrincipalSecurityContextFactory.class)
public @interface WithMockUserPrincipal {

    /**
     * User ID
     */
    String userId() default "ffffffff-ffff-ffff-ffff-ffffffffffff";

    /**
     * User email
     */
    String email() default "test@example.com";

    /**
     * User nickname
     */
    String nickname() default "테스트사용자";

    /**
     * User roles
     */
    String[] roles() default {"ROLE_USER"};

    /**
     * Time zone
     */
    String timeZone() default "Asia/Seoul";

    /**
     * Locale
     */
    String locale() default "ko_KR";
}