package point.zzicback.todo.presentation.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import point.zzicback.todo.presentation.dto.request.UpdateTodoRequest;

public class CompleteUpdateValidator implements ConstraintValidator<ValidCompleteUpdate, UpdateTodoRequest> {
    
    @Override
    public boolean isValid(UpdateTodoRequest request, ConstraintValidatorContext context) {
        if (request == null || request.getComplete() == null) {
            return true; // 완료 상태가 없으면 검증 통과
        }
        
        // 완료 상태가 있을 때, 다른 필드들이 모두 비어있는지 확인
        boolean hasOtherFields = 
            (request.getTitle() != null && !request.getTitle().trim().isEmpty()) ||
            (request.getDescription() != null && !request.getDescription().trim().isEmpty()) ||
            request.getPriorityId() != null ||
            request.getCategoryId() != null ||
            request.getDate() != null ||
            request.getTime() != null ||
            request.getRepeatType() != null ||
            request.getRepeatInterval() != null ||
            request.getRepeatStartDate() != null ||
            request.getRepeatEndDate() != null ||
            (request.getDaysOfWeek() != null && !request.getDaysOfWeek().isEmpty()) ||
            (request.getTags() != null && !request.getTags().isEmpty());
            
        return !hasOtherFields; // 다른 필드가 없으면 true (검증 통과)
    }
}
