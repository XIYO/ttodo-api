package point.ttodoApi.profile.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;

/**
 * 이미지 타입 유효성 검증 애노테이션
 * - 선택값 (null 허용)
 * - 최대 50자
 * - 지원 형식: image/jpeg, image/jpg, image/png, image/gif, image/webp
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Size(max = 50, message = "이미지 타입은 50자 이하여야 합니다")
@Pattern(regexp = "^(image/(jpeg|jpg|png|gif|webp))?$", 
         message = "지원하지 않는 이미지 형식입니다", 
         flags = Pattern.Flag.CASE_INSENSITIVE)
@Constraint(validatedBy = {})
public @interface ValidImageType {
    String message() default "올바른 이미지 타입이 아닙니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}