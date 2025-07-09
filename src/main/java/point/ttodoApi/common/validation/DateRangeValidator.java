package point.ttodoApi.common.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import point.ttodoApi.todo.presentation.dto.request.TodoSearchRequest;

public class DateRangeValidator implements ConstraintValidator<DateRangeConstraint, TodoSearchRequest> {
    
    @Override
    public void initialize(DateRangeConstraint constraintAnnotation) {
        // 초기화 로직이 필요하면 여기에 작성
    }
    
    @Override
    public boolean isValid(TodoSearchRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }
        
        // startDate와 endDate는 둘 다 있거나 둘 다 없어야 함
        boolean hasStartDate = request.startDate() != null;
        boolean hasEndDate = request.endDate() != null;
        
        return hasStartDate == hasEndDate;
    }
}