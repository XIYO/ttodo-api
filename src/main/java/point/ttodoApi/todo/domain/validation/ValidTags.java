package point.ttodoApi.todo.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 태그 유효성 검증 애노테이션
 * - 선택값 (null 허용)
 * - 각 태그 최대 50자
 * - 최대 20개 태그
 * - 빈 문자열 태그 불허
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = TagsValidator.class)
public @interface ValidTags {
    String message() default "올바른 태그 형식이 아닙니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
