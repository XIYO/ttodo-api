package point.ttodoApi.shared.validation;

import jakarta.validation.*;
import point.ttodoApi.todo.presentation.dto.request.TodoSearchRequest;

public class DateRangeValidator implements ConstraintValidator<DateRangeConstraint, TodoSearchRequest> {

  @Override
  public void initialize(DateRangeConstraint constraintAnnotation) {
    // 초기화 로직이 필요하면 여기에 작성
  }

  @Override
  public boolean isValid(TodoSearchRequest request, ConstraintValidatorContext context) {
    // dates 파라미터는 별도 검증이 필요없음
    // 1개, 2개, 3개 이상 모두 유효한 케이스
    return true;
  }
}