package point.ttodoApi.auth.domain.validation.required;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import jakarta.validation.constraints.NotBlank;

import java.lang.annotation.*;

/**
 * TTODO 아키텍처 패턴: 필수 디바이스 ID 검증 어노테이션
 * 복합 검증: NotNull + NotBlank
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = {})
@NotBlank(message = "디바이스 ID는 필수입니다")
@Documented
public @interface ValidDeviceId {
    String message() default "유효하지 않은 디바이스 ID입니다";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}