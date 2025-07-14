package point.ttodoApi.todo.exception;

import point.ttodoApi.common.error.DataNotFoundException;
import point.ttodoApi.common.error.ErrorCode;

import java.util.UUID;

public class TodoNotFoundException extends DataNotFoundException {
    
    public TodoNotFoundException(UUID todoId) {
        super(ErrorCode.TODO_NOT_FOUND, 
              String.format("할일(ID: %s)을 찾을 수 없습니다.", todoId));
    }
    
    public TodoNotFoundException(Long todoId) {
        super(ErrorCode.TODO_NOT_FOUND, 
              String.format("할일(ID: %d)을 찾을 수 없습니다.", todoId));
    }
    
    public TodoNotFoundException() {
        super(ErrorCode.TODO_NOT_FOUND);
    }
}