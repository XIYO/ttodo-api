package point.ttodoApi.todo.exception;

import point.ttodoApi.shared.error.*;

import java.time.LocalDate;

public class InvalidTodoDateException extends InvalidRequestException {
    
    public InvalidTodoDateException(LocalDate date) {
        super(ErrorCode.INVALID_TODO_DATE, 
              String.format("할일 날짜 '%s'은(는) 유효하지 않습니다.", date));
    }
    
    public InvalidTodoDateException(String message) {
        super(ErrorCode.INVALID_TODO_DATE, message);
    }
    
    public InvalidTodoDateException() {
        super(ErrorCode.INVALID_TODO_DATE);
    }
}