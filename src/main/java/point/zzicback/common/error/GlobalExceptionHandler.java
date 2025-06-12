package point.zzicback.common.error;

import org.springframework.http.*;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(EntityNotFoundException.class)
  public ProblemDetail handleEntityNotFound(EntityNotFoundException ex) {
    ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
    detail.setTitle("Entity Not Found");
    detail.setDetail(ex.getMessage());
    return detail;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
    ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    detail.setTitle("Validation Failed");
    Map<String, List<String>> fieldErrors = ex.getBindingResult().getFieldErrors().stream().collect(Collectors
            .groupingBy(FieldError::getField, Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())));
    detail.setProperty("errors", fieldErrors);
    return detail;
  }

  @ExceptionHandler(BusinessException.class)
  public ProblemDetail handleBusinessException(BusinessException ex) {
    ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    detail.setTitle("요청 처리 실패");
    detail.setDetail(ex.getMessage());
    return detail;
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
    ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    detail.setTitle("Bad Request");
    detail.setDetail(ex.getMessage());
    return detail;
  }



  @ExceptionHandler(Exception.class)
  public ProblemDetail handleGeneric(Exception ex) {
    // 에러 로깅 추가
    System.err.println("Unexpected error occurred: " + ex.getClass().getSimpleName() + " - " + ex.getMessage());
    ex.printStackTrace();
    
    ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    detail.setTitle("Internal Server Error");
    detail.setDetail("서버 내부 오류가 발생했습니다");
    return detail;
  }
}
