package point.ttodoApi.category.domain.validation.optional;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;

import java.lang.annotation.*;

/**
 * TTODO 아키텍처 패턴: 선택적 카테고리 색상 검증 어노테이션
 * 선택적이지만 있을 경우 HEX 색상 형식 검증
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Pattern(regexp = "^#[0-9A-Fa-f]{6}$", message = "색상은 HEX 형식이어야 합니다 (예: #FF5733)")
@Documented
public @interface OptionalCategoryColor {
    String message() default "유효하지 않은 카테고리 색상입니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}