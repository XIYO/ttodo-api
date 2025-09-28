package point.ttodoApi.category.domain.validation.required;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * TTODO 아키텍처 패턴: 필수 카테고리 ID 검증 어노테이션
 * UUID 형식 검증
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Documented
public @interface ValidCategoryId {
    String message() default "유효하지 않은 카테고리 ID입니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}