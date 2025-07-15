package point.ttodoApi.test.security;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.*;

/**
 * 테스트에서 anon@ttodo.dev 사용자로 인증하기 위한 애너테이션
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithAnonUserSecurityContextFactory.class)
public @interface WithAnonUser {
}