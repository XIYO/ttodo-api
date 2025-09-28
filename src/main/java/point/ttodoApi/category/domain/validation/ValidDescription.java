package point.ttodoApi.category.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;

/**
 * 카테고리 설명 유효성 검증 애노테이션
 * - 선택값 (null 허용)
 * - 최대 255자
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Size(max = 255, message = "설명은 255자 이하여야 합니다")
@Constraint(validatedBy = {})
public @interface ValidDescription {
    String message() default "올바른 설명 형식이 아닙니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
