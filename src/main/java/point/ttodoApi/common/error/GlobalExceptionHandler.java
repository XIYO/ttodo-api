package point.ttodoApi.common.error;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Slf4j
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

  @ExceptionHandler(NotFoundException.class)
  public ProblemDetail handleNotFoundException(NotFoundException ex) {
    log.warn("리소스를 찾을 수 없음 - errorCode: {}, message: {}", ex.getErrorCode(), ex.getMessage());
    
    ProblemDetail detail = ProblemDetail.forStatus(ex.getHttpStatus());
    detail.setTitle("리소스를 찾을 수 없음");
    detail.setDetail(ex.getMessage());
    detail.setProperty("timestamp", new Date());
    detail.setProperty("errorCode", ex.getErrorCode());
    return detail;
  }

  @ExceptionHandler(ConflictException.class)
  public ProblemDetail handleConflictException(ConflictException ex) {
    log.warn("리소스 충돌 발생 - errorCode: {}, message: {}", ex.getErrorCode(), ex.getMessage());
    
    ProblemDetail detail = ProblemDetail.forStatus(ex.getHttpStatus());
    detail.setTitle("리소스 충돌");
    detail.setDetail(ex.getMessage());
    detail.setProperty("timestamp", new Date());
    detail.setProperty("errorCode", ex.getErrorCode());
    return detail;
  }

  @ExceptionHandler(ForbiddenException.class)
  public ProblemDetail handleForbiddenException(ForbiddenException ex) {
    log.warn("접근 권한 없음 - errorCode: {}, message: {}", ex.getErrorCode(), ex.getMessage());
    
    ProblemDetail detail = ProblemDetail.forStatus(ex.getHttpStatus());
    detail.setTitle("접근 권한 없음");
    detail.setDetail(ex.getMessage());
    detail.setProperty("timestamp", new Date());
    detail.setProperty("errorCode", ex.getErrorCode());
    return detail;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleValidation(MethodArgumentNotValidException ex) {
    ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
    detail.setTitle("입력값 검증 실패");
    detail.setDetail("요청 데이터가 유효하지 않습니다. 아래 오류를 확인해주세요.");
    
    // 필드별 에러 메시지 수집
    Map<String, String> fieldErrors = new HashMap<>();
    for (FieldError error : ex.getBindingResult().getFieldErrors()) {
        fieldErrors.put(error.getField(), error.getDefaultMessage());
    }
    
    // ProblemDetail 표준 구조로 통일
    detail.setProperty("timestamp", new Date());
    detail.setProperty("errorCode", "VALIDATION_ERROR");
    detail.setProperty("errors", fieldErrors);
    
    return detail;
  }

  @ExceptionHandler(BusinessException.class)
  public ProblemDetail handleBusinessException(BusinessException ex) {
    log.warn("비즈니스 로직 예외 - errorCode: {}, status: {}, message: {}", 
            ex.getErrorCode(), ex.getHttpStatus(), ex.getMessage());
    
    ProblemDetail detail = ProblemDetail.forStatus(ex.getHttpStatus());
    detail.setTitle("요청 처리 실패");
    detail.setDetail(ex.getMessage());
    detail.setProperty("timestamp", new Date());
    detail.setProperty("errorCode", ex.getErrorCode());
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
    // 상세한 에러 로깅
    log.error("=== 예상치 못한 오류 발생 ===");
    log.error("예외 타입: {}", ex.getClass().getSimpleName());
    log.error("예외 메시지: {}", ex.getMessage());
    log.error("스택 트레이스:", ex);
    
    // 원인 예외도 로깅
    Throwable cause = ex.getCause();
    if (cause != null) {
      log.error("원인 예외 타입: {}", cause.getClass().getSimpleName());
      log.error("원인 예외 메시지: {}", cause.getMessage());
    }
    
    ProblemDetail detail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
    detail.setTitle("서버 내부 오류");
    detail.setDetail("서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
    detail.setProperty("timestamp", new Date());
    detail.setProperty("errorCode", "INTERNAL_SERVER_ERROR");
    
    // 개발 환경에서는 더 자세한 정보 제공
    if (log.isDebugEnabled()) {
      detail.setProperty("exceptionType", ex.getClass().getSimpleName());
      detail.setProperty("exceptionMessage", ex.getMessage());
    }
    
    return detail;
  }
}
