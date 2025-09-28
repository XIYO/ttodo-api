package point.ttodoApi.category.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * 색상 유효성 검증 애노테이션
 * - 선택값 (null 허용)
 * - #RRGGBB 형식 (Hex 색상 코드)
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = ColorValidator.class)
public @interface ValidColor {
    String message() default "색상은 #RRGGBB 형식이어야 합니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
