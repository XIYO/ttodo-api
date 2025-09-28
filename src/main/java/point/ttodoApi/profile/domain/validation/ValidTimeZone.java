package point.ttodoApi.profile.domain.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.lang.annotation.*;

/**
 * 타임존 유효성 검증 애노테이션
 * - 필수값
 * - 최대 50자
 * - 유효한 타임존 형식 (예: Asia/Seoul, America/New_York)
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@NotBlank(message = "타임존은 필수입니다")
@Size(max = 50, message = "타임존은 50자 이하여야 합니다")
@Constraint(validatedBy = TimeZoneValidator.class)
public @interface ValidTimeZone {
    String message() default "유효하지 않은 타임존입니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}