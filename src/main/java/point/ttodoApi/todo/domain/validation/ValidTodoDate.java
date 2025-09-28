package point.ttodoApi.todo.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Todo 날짜 유효성 검증 애노테이션
 * - 선택값 (null 허용)
 * - 과거 날짜 불허 (오늘 날짜는 허용)
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = TodoDateValidator.class)
public @interface ValidTodoDate {
    String message() default "올바른 날짜 형식이 아닙니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    
    /**
     * 과거 날짜 허용 여부 (기본: false)
     */
    boolean allowPast() default false;
}
