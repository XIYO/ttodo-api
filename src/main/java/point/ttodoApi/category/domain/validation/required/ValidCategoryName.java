package point.ttodoApi.category.domain.validation.required;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;

/**
 * TTODO 아키텍처 패턴: 필수 카테고리명 검증 어노테이션
 * 1-50자 제한 및 빈 문자열 방지
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@NotBlank(message = "카테고리명은 필수입니다")
@Size(min = 1, max = 50, message = "카테고리명은 1-50자 사이여야 합니다")
@Documented
public @interface ValidCategoryName {
    String message() default "유효하지 않은 카테고리명입니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}