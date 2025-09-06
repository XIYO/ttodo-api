package point.ttodoApi.test.security;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.*;

/**
 * 테스트에서 MemberPrincipal을 사용하기 위한 애너테이션
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockMemberPrincipalSecurityContextFactory.class)
public @interface WithMockMemberPrincipal {
  String memberId() default "550e8400-e29b-41d4-a716-446655440000";

  String email() default "test@example.com";

  String nickname() default "테스트유저";

  String[] roles() default {"USER"};
}