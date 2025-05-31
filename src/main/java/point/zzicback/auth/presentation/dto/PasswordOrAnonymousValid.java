package point.zzicback.auth.presentation.dto;

import jakarta.validation.*;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PasswordOrAnonymousValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface PasswordOrAnonymousValid {
String message() default "비밀번호는 필수 입력 항목입니다.";

Class<?>[] groups() default {};

Class<? extends Payload>[] payload() default {};
}
