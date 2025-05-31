package point.zzicback.common.validation.fieldcompare;

import jakarta.validation.*;

import java.lang.reflect.RecordComponent;

public class FieldComparisonValidator implements ConstraintValidator<FieldComparison, Object> {
@Override
public boolean isValid(Object value, ConstraintValidatorContext context) {
  // null 객체는 다른 유효성 검증(@NotBlank 등)에 맡김
  if (value == null) {
    return true;
  }
  try {
    Object targetValue = null;
    Object resultValue = null;
    String targetName = null;
    String resultName = null;
    // record 컴포넌트만 순회
    for (RecordComponent rc : value.getClass().getRecordComponents()) {
      if (rc.isAnnotationPresent(CompareTarget.class)) {
        targetValue = rc.getAccessor().invoke(value);
        targetName = rc.getName();
      }
      if (rc.isAnnotationPresent(CompareResult.class)) {
        resultValue = rc.getAccessor().invoke(value);
        resultName = rc.getName();
      }
    }
    // 비교할 record 컴포넌트가 없으면 통과하도록 함
    if (targetName == null || resultName == null) {
      return true;
    }
    // 두 값 모두 null이면 검증 통과
    if (targetValue == null && resultValue == null) {
      return true;
    }
    // 둘 중 하나만 null이거나 값이 다르면 검증 실패
    if (targetValue == null || ! targetValue.equals(resultValue)) {
      context.disableDefaultConstraintViolation();
      context.buildConstraintViolationWithTemplate(
                      String.format("'%s' 필드와 '%s' 필드가 같지 않습니다.", targetName, resultName)).addPropertyNode(resultName)
              .addConstraintViolation();
      return false;
    }
    return true;
  } catch (Exception e) {
    throw new RuntimeException("Reflection 오류: record 컴포넌트 접근에 실패했습니다.", e);
  }
}
}
