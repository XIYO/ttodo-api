package point.ttodoApi.todo.presentation.validation;

import jakarta.validation.*;
import point.ttodoApi.todo.presentation.dto.request.UpdateTodoRequest;

public class CompleteUpdateValidator implements ConstraintValidator<ValidCompleteUpdate, UpdateTodoRequest> {
    
    @Override
    public boolean isValid(UpdateTodoRequest request, ConstraintValidatorContext context) {
        if (request == null || request.complete() == null) {
            return true; // 완료 상태가 없으면 검증 통과
        }
        
        // 완료 상태가 있을 때, 다른 필드들이 모두 비어있는지 확인
        boolean hasOtherFields = 
            (request.title() != null && !request.title().trim().isEmpty()) ||
            (request.description() != null && !request.description().trim().isEmpty()) ||
            request.priorityId() != null ||
            request.categoryId() != null ||
            request.date() != null ||
            request.time() != null ||
            request.recurrenceRuleJson() != null ||
            (request.tags() != null && !request.tags().isEmpty());
            
        return !hasOtherFields; // 다른 필드가 없으면 true (검증 통과)
    }
}
