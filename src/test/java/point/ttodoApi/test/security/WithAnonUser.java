package point.ttodoApi.test.security;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Test annotation for mocking anonymous user (anon@ttodo.dev)
 *
 * Usage:
 * @WithAnonUser
 */
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithAnonUserSecurityContextFactory.class)
public @interface WithAnonUser {

    /**
     * User nickname for anonymous user
     */
    String nickname() default "익명사용자";

    /**
     * Time zone for anonymous user
     */
    String timeZone() default "Asia/Seoul";

    /**
     * Locale for anonymous user
     */
    String locale() default "ko_KR";
}