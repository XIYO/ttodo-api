package point.ttodoApi.common.validation.fieldcompare;

import jakarta.validation.*;

import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = FieldComparisonValidator.class)
@Documented
public @interface FieldComparison {
  String message() default "필드 값이 일치하지 않습니다.";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
