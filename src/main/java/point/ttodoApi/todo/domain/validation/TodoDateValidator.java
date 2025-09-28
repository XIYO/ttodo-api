package point.ttodoApi.todo.domain.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDate;

import static point.ttodoApi.todo.domain.TodoConstants.DATE_FUTURE_MESSAGE;

/**
 * Todo 날짜 유효성 검증 구현
 */
public class TodoDateValidator implements ConstraintValidator<ValidTodoDate, LocalDate> {
    
    private boolean allowPast;
    
    @Override
    public void initialize(ValidTodoDate constraintAnnotation) {
        this.allowPast = constraintAnnotation.allowPast();
    }
    
    @Override
    public boolean isValid(LocalDate date, ConstraintValidatorContext context) {
        // null인 경우 유효 (선택값)
        if (date == null) {
            return true;
        }
        
        // 과거 날짜 허용 여부에 따른 검증
        if (!allowPast && date.isBefore(LocalDate.now())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate(DATE_FUTURE_MESSAGE)
                    .addConstraintViolation();
            return false;
        }
        
        return true;
    }
}
