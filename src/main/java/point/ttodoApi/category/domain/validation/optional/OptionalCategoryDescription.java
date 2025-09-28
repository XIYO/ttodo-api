package point.ttodoApi.category.domain.validation.optional;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;

/**
 * TTODO 아키텍처 패턴: 선택적 카테고리 설명 검증 어노테이션
 * 선택적이지만 있을 경우 길이 제한 적용
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@Size(max = 200, message = "카테고리 설명은 200자 이하여야 합니다")
@Documented
public @interface OptionalCategoryDescription {
    String message() default "유효하지 않은 카테고리 설명입니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}