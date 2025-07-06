package point.zzicback.common.error;

import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(EntityNotFoundException.class)
  public ProblemDetail handleEntityNotFound(EntityNotFoundException ex) {
    ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
    detail.setTitle("리소스를 찾을 수 없음");
    detail.setDetail(ex.getMessage());
    detail.setProperty("timestamp", new Date());
    detail.setProperty("errorCode", "RESOURCE_NOT_FOUND");
    return detail;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
    ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    detail.setTitle("입력값 검증 실패");
    detail.setDetail("요청 데이터가 유효하지 않습니다. 아래 오류를 확인해주세요.");
    
    // 필드별 에러 메시지 수집
    Map<String, List<String>> fieldErrors = ex.getBindingResult().getFieldErrors().stream()
            .collect(Collectors.groupingBy(
                    FieldError::getField, 
                    Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())
            ));
    
    detail.setProperty("errors", fieldErrors);
    detail.setProperty("timestamp", new Date());
    detail.setProperty("errorCode", "VALIDATION_ERROR");
    
    return detail;
  }

  @ExceptionHandler(BusinessException.class)
  public ProblemDetail handleBusinessException(BusinessException ex) {
    ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    detail.setTitle("요청 처리 실패");
    detail.setDetail(ex.getMessage());
    detail.setProperty("timestamp", new Date());
    detail.setProperty("errorCode", "BUSINESS_ERROR");
    return detail;
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
    ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    detail.setTitle("잘못된 요청");
    detail.setDetail(ex.getMessage());
    detail.setProperty("timestamp", new Date());
    detail.setProperty("errorCode", "INVALID_ARGUMENT");
    return detail;
  }
  
  @ExceptionHandler(AuthenticationException.class)
  public ProblemDetail handleAuthenticationException(AuthenticationException ex) {
    ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
    detail.setTitle("인증 실패");
    detail.setDetail("인증이 필요합니다. 유효한 인증 정보를 제공해주세요.");
    detail.setProperty("timestamp", new Date());
    detail.setProperty("errorCode", "AUTHENTICATION_FAILED");
    return detail;
  }
  
  @ExceptionHandler(AccessDeniedException.class)
  public ProblemDetail handleAccessDeniedException(AccessDeniedException ex) {
    ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
    detail.setTitle("접근 권한 없음");
    detail.setDetail("해당 리소스에 접근할 권한이 없습니다.");
    detail.setProperty("timestamp", new Date());
    detail.setProperty("errorCode", "ACCESS_DENIED");
    return detail;
  }
  
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ProblemDetail handleMissingServletRequestParameter(MissingServletRequestParameterException ex) {
    ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    detail.setTitle("필수 파라미터 누락");
    detail.setDetail(String.format("필수 파라미터 '%s'이(가) 누락되었습니다.", ex.getParameterName()));
    detail.setProperty("timestamp", new Date());
    detail.setProperty("errorCode", "MISSING_PARAMETER");
    detail.setProperty("parameterName", ex.getParameterName());
    detail.setProperty("parameterType", ex.getParameterType());
    return detail;
  }
  
  @ExceptionHandler(MissingServletRequestPartException.class)
  public ProblemDetail handleMissingServletRequestPart(MissingServletRequestPartException ex) {
    ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    detail.setTitle("필수 파일 누락");
    detail.setDetail(String.format("필수 파일 파라미터 '%s'이(가) 누락되었습니다.", ex.getRequestPartName()));
    detail.setProperty("timestamp", new Date());
    detail.setProperty("errorCode", "MISSING_FILE");
    detail.setProperty("requestPartName", ex.getRequestPartName());
    return detail;
  }



  @ExceptionHandler(Exception.class)
  public ProblemDetail handleGeneric(Exception ex) {
    // 에러 로깅 추가
    System.err.println("Unexpected error occurred: " + ex.getClass().getSimpleName() + " - " + ex.getMessage());
    ex.printStackTrace();
    
    ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    detail.setTitle("서버 내부 오류");
    detail.setDetail("서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
    detail.setProperty("timestamp", new Date());
    detail.setProperty("errorCode", "INTERNAL_SERVER_ERROR");
    return detail;
  }
}
