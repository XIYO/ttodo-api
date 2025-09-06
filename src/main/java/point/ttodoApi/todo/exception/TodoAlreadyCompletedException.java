package point.ttodoApi.todo.exception;

import point.ttodoApi.shared.error.*;

public class TodoAlreadyCompletedException extends BaseException {

  public TodoAlreadyCompletedException(String todoTitle) {
    super(ErrorCode.TODO_ALREADY_COMPLETED,
            String.format("할일 '%s'은(는) 이미 완료되었습니다.", todoTitle));
  }

  public TodoAlreadyCompletedException() {
    super(ErrorCode.TODO_ALREADY_COMPLETED);
  }
}