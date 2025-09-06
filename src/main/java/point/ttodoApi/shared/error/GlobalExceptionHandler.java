package point.ttodoApi.shared.error;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.net.URI;
import java.nio.file.AccessDeniedException;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * RFC 7807 Problem Details를 사용하는 전역 예외 처리기
 * Spring Boot 3의 ProblemDetail을 활용한 표준 에러 응답 처리
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  private final ErrorMetricsCollector metricsCollector;

  public GlobalExceptionHandler(ErrorMetricsCollector metricsCollector) {
    this.metricsCollector = metricsCollector;
  }

  @ExceptionHandler(BaseException.class)
  public ProblemDetail handleBaseException(BaseException ex, HttpServletRequest request) {
    log.warn("Business exception occurred - errorCode: {}, status: {}, message: {}",
            ex.getErrorCode(), ex.getHttpStatus(), ex.getMessage());

    metricsCollector.recordError(ex.getErrorCode(), ex.getHttpStatus().value(), ex.getClass().getSimpleName());

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(ex.getHttpStatus(), ex.getMessage());
    problemDetail.setType(URI.create(buildErrorType(ex.getErrorCode())));
    problemDetail.setTitle(getErrorTitle(ex.getErrorCode()));
    problemDetail.setInstance(URI.create(request.getRequestURI()));
    problemDetail.setProperty("errorCode", ex.getErrorCode());
    problemDetail.setProperty("timestamp", Instant.now());

    return problemDetail;
  }

  @ExceptionHandler(BusinessException.class)
  public ProblemDetail handleBusinessException(BusinessException ex, HttpServletRequest request) {
    log.warn("Business exception occurred - errorCode: {}, status: {}, message: {}",
            ex.getErrorCodeValue(), ex.getHttpStatus(), ex.getMessage());

    metricsCollector.recordError(ex.getErrorCodeValue(), ex.getHttpStatus().value(), ex.getClass().getSimpleName());

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(ex.getHttpStatus(), ex.getMessage());
    problemDetail.setType(URI.create(buildErrorType(ex.getErrorCode())));
    problemDetail.setTitle(ex.getErrorCode().getMessage());
    problemDetail.setInstance(URI.create(request.getRequestURI()));
    problemDetail.setProperty("errorCode", ex.getErrorCodeValue());
    problemDetail.setProperty("timestamp", Instant.now());

    return problemDetail;
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ProblemDetail handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
    Map<String, String> fieldErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.toMap(
                    FieldError::getField,
                    error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value",
                    (existing, replacement) -> existing
            ));

    fieldErrors.keySet().forEach(field -> metricsCollector.recordValidationError(field));

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "요청 데이터가 유효하지 않습니다."
    );
    problemDetail.setType(URI.create("/errors/validation-error"));
    problemDetail.setTitle("입력값 검증 실패");
    problemDetail.setInstance(URI.create(request.getRequestURI()));
    problemDetail.setProperty("errorCode", ErrorCode.VALIDATION_ERROR.getCode());
    problemDetail.setProperty("timestamp", Instant.now());
    problemDetail.setProperty("errors", fieldErrors);

    return problemDetail;
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ProblemDetail handleMissingServletRequestParameter(
          MissingServletRequestParameterException ex, HttpServletRequest request) {

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            String.format("필수 파라미터 '%s'이(가) 누락되었습니다.", ex.getParameterName())
    );
    problemDetail.setType(URI.create("/errors/missing-parameter"));
    problemDetail.setTitle("필수 파라미터 누락");
    problemDetail.setInstance(URI.create(request.getRequestURI()));
    problemDetail.setProperty("errorCode", ErrorCode.MISSING_PARAMETER.getCode());
    problemDetail.setProperty("timestamp", Instant.now());
    problemDetail.setProperty("parameterName", ex.getParameterName());
    problemDetail.setProperty("parameterType", ex.getParameterType());

    return problemDetail;
  }

  @ExceptionHandler(MissingServletRequestPartException.class)
  public ProblemDetail handleMissingServletRequestPart(
          MissingServletRequestPartException ex, HttpServletRequest request) {

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            String.format("필수 파일 파라미터 '%s'이(가) 누락되었습니다.", ex.getRequestPartName())
    );
    problemDetail.setType(URI.create("/errors/missing-file"));
    problemDetail.setTitle("필수 파일 누락");
    problemDetail.setInstance(URI.create(request.getRequestURI()));
    problemDetail.setProperty("errorCode", ErrorCode.MISSING_FILE.getCode());
    problemDetail.setProperty("timestamp", Instant.now());
    problemDetail.setProperty("requestPartName", ex.getRequestPartName());

    return problemDetail;
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ProblemDetail handleMethodArgumentTypeMismatch(
          MethodArgumentTypeMismatchException ex, HttpServletRequest request) {

    String detail = String.format("파라미터 '%s'의 값 '%s'은(는) %s 타입으로 변환할 수 없습니다.",
            ex.getName(), ex.getValue(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "required");

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, detail);
    problemDetail.setType(URI.create("/errors/invalid-argument"));
    problemDetail.setTitle("잘못된 파라미터 타입");
    problemDetail.setInstance(URI.create(request.getRequestURI()));
    problemDetail.setProperty("errorCode", ErrorCode.INVALID_ARGUMENT.getCode());
    problemDetail.setProperty("timestamp", Instant.now());

    return problemDetail;
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ProblemDetail handleHttpRequestMethodNotSupported(
          HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.METHOD_NOT_ALLOWED,
            String.format("'%s' 메소드는 이 엔드포인트에서 지원되지 않습니다.", ex.getMethod())
    );
    problemDetail.setType(URI.create("/errors/method-not-allowed"));
    problemDetail.setTitle("허용되지 않은 메소드");
    problemDetail.setInstance(URI.create(request.getRequestURI()));
    problemDetail.setProperty("errorCode", ErrorCode.METHOD_NOT_ALLOWED.getCode());
    problemDetail.setProperty("timestamp", Instant.now());
    problemDetail.setProperty("supportedMethods", ex.getSupportedHttpMethods() != null ? ex.getSupportedHttpMethods() : Collections.emptySet());

    return problemDetail;
  }

  @ExceptionHandler(NoHandlerFoundException.class)
  public ProblemDetail handleNoHandlerFoundException(
          NoHandlerFoundException ex, HttpServletRequest request) {

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            String.format("요청한 URL '%s'에 해당하는 엔드포인트를 찾을 수 없습니다.", ex.getRequestURL())
    );
    problemDetail.setType(URI.create("/errors/resource-not-found"));
    problemDetail.setTitle("엔드포인트를 찾을 수 없음");
    problemDetail.setInstance(URI.create(request.getRequestURI()));
    problemDetail.setProperty("errorCode", ErrorCode.RESOURCE_NOT_FOUND.getCode());
    problemDetail.setProperty("timestamp", Instant.now());

    return problemDetail;
  }

  @ExceptionHandler(MaxUploadSizeExceededException.class)
  public ProblemDetail handleMaxUploadSizeExceeded(
          MaxUploadSizeExceededException ex, HttpServletRequest request) {

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "업로드 파일 크기가 허용된 최대 크기를 초과했습니다."
    );
    problemDetail.setType(URI.create("/errors/file-size-exceeded"));
    problemDetail.setTitle("파일 크기 초과");
    problemDetail.setInstance(URI.create(request.getRequestURI()));
    problemDetail.setProperty("errorCode", ErrorCode.FILE_SIZE_EXCEEDED.getCode());
    problemDetail.setProperty("timestamp", Instant.now());
    problemDetail.setProperty("maxSize", ex.getMaxUploadSize());

    return problemDetail;
  }

  @ExceptionHandler(AuthenticationException.class)
  public ProblemDetail handleAuthenticationException(
          AuthenticationException ex, HttpServletRequest request) {

    metricsCollector.recordAuthenticationFailure(ex.getClass().getSimpleName());

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.UNAUTHORIZED,
            "인증이 필요합니다. 유효한 인증 정보를 제공해주세요."
    );
    problemDetail.setType(URI.create("/errors/authentication-required"));
    problemDetail.setTitle("인증 필요");
    problemDetail.setInstance(URI.create(request.getRequestURI()));
    problemDetail.setProperty("errorCode", ErrorCode.AUTHENTICATION_FAILED.getCode());
    problemDetail.setProperty("timestamp", Instant.now());

    return problemDetail;
  }

  @ExceptionHandler(AccessDeniedException.class)
  public ProblemDetail handleAccessDeniedException(
          AccessDeniedException ex, HttpServletRequest request) {

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.FORBIDDEN,
            "해당 리소스에 접근할 권한이 없습니다."
    );
    problemDetail.setType(URI.create("/errors/access-denied"));
    problemDetail.setTitle("접근 권한 없음");
    problemDetail.setInstance(URI.create(request.getRequestURI()));
    problemDetail.setProperty("errorCode", ErrorCode.ACCESS_DENIED.getCode());
    problemDetail.setProperty("timestamp", Instant.now());

    return problemDetail;
  }

  @ExceptionHandler(AuthorizationDeniedException.class)
  public ProblemDetail handleAuthorizationDeniedException(
          AuthorizationDeniedException ex, HttpServletRequest request) {

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.FORBIDDEN,
            "해당 리소스에 접근할 권한이 없습니다."
    );
    problemDetail.setType(URI.create("/errors/authorization-denied"));
    problemDetail.setTitle("권한 거부");
    problemDetail.setInstance(URI.create(request.getRequestURI()));
    problemDetail.setProperty("errorCode", ErrorCode.ACCESS_DENIED.getCode());
    problemDetail.setProperty("timestamp", Instant.now());

    return problemDetail;
  }

  @ExceptionHandler(IllegalArgumentException.class)
  public ProblemDetail handleIllegalArgument(
          IllegalArgumentException ex, HttpServletRequest request) {

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            ex.getMessage()
    );
    problemDetail.setType(URI.create("/errors/invalid-argument"));
    problemDetail.setTitle("잘못된 인자");
    problemDetail.setInstance(URI.create(request.getRequestURI()));
    problemDetail.setProperty("errorCode", ErrorCode.INVALID_ARGUMENT.getCode());
    problemDetail.setProperty("timestamp", Instant.now());

    return problemDetail;
  }

  @ExceptionHandler(Exception.class)
  public ProblemDetail handleGeneric(Exception ex, HttpServletRequest request) {
    log.error("Unexpected error occurred", ex);

    metricsCollector.recordError(ErrorCode.INTERNAL_SERVER_ERROR.getCode(),
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            ex.getClass().getSimpleName());

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
    );
    problemDetail.setType(URI.create("/errors/internal-server-error"));
    problemDetail.setTitle("서버 내부 오류");
    problemDetail.setInstance(URI.create(request.getRequestURI()));
    problemDetail.setProperty("errorCode", ErrorCode.INTERNAL_SERVER_ERROR.getCode());
    problemDetail.setProperty("timestamp", Instant.now());

    if (log.isDebugEnabled()) {
      problemDetail.setProperty("exceptionType", ex.getClass().getSimpleName());
      problemDetail.setProperty("exceptionMessage", ex.getMessage() != null ? ex.getMessage() : "No message");
    }

    return problemDetail;
  }

  // Legacy exception handlers for backward compatibility
  @ExceptionHandler(EntityNotFoundException.class)
  public ProblemDetail handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest request) {
    log.warn("Entity not found - message: {}", ex.getMessage());

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND,
            ex.getMessage()
    );
    problemDetail.setType(URI.create("/errors/entity-not-found"));
    problemDetail.setTitle("엔티티를 찾을 수 없음");
    problemDetail.setInstance(URI.create(request.getRequestURI()));
    problemDetail.setProperty("errorCode", ErrorCode.RESOURCE_NOT_FOUND.getCode());
    problemDetail.setProperty("timestamp", Instant.now());

    return problemDetail;
  }

  @ExceptionHandler(NotFoundException.class)
  public ProblemDetail handleNotFoundException(NotFoundException ex, HttpServletRequest request) {
    log.warn("Resource not found - errorCode: {}, message: {}", ex.getErrorCodeValue(), ex.getMessage());

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(ex.getHttpStatus(), ex.getMessage());
    problemDetail.setType(URI.create(buildErrorType(ex.getErrorCode())));
    problemDetail.setTitle("리소스를 찾을 수 없음");
    problemDetail.setInstance(URI.create(request.getRequestURI()));
    problemDetail.setProperty("errorCode", ex.getErrorCodeValue());
    problemDetail.setProperty("timestamp", Instant.now());

    return problemDetail;
  }

  @ExceptionHandler(ConflictException.class)
  public ProblemDetail handleConflictException(ConflictException ex, HttpServletRequest request) {
    log.warn("Resource conflict - errorCode: {}, message: {}", ex.getErrorCodeValue(), ex.getMessage());

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(ex.getHttpStatus(), ex.getMessage());
    problemDetail.setType(URI.create(buildErrorType(ex.getErrorCode())));
    problemDetail.setTitle("리소스 충돌");
    problemDetail.setInstance(URI.create(request.getRequestURI()));
    problemDetail.setProperty("errorCode", ex.getErrorCodeValue());
    problemDetail.setProperty("timestamp", Instant.now());

    return problemDetail;
  }

  @ExceptionHandler(ForbiddenException.class)
  public ProblemDetail handleForbiddenException(ForbiddenException ex, HttpServletRequest request) {
    log.warn("Access forbidden - errorCode: {}, message: {}", ex.getErrorCodeValue(), ex.getMessage());

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(ex.getHttpStatus(), ex.getMessage());
    problemDetail.setType(URI.create(buildErrorType(ex.getErrorCode())));
    problemDetail.setTitle("접근 권한 없음");
    problemDetail.setInstance(URI.create(request.getRequestURI()));
    problemDetail.setProperty("errorCode", ex.getErrorCodeValue());
    problemDetail.setProperty("timestamp", Instant.now());

    return problemDetail;
  }

  private String buildErrorType(String errorCode) {
    return "/errors/" + errorCode.toLowerCase().replace("_", "-");
  }

  private String buildErrorType(ErrorCode errorCode) {
    return "/errors/" + errorCode.getCode().toLowerCase().replace("_", "-");
  }

  private String getErrorTitle(String errorCode) {
    try {
      ErrorCode code = ErrorCode.valueOf(errorCode.replace("-", "_").toUpperCase());
      return code.getMessage();
    } catch (IllegalArgumentException e) {
      return "오류 발생";
    }
  }

  private String getErrorTitle(ErrorCode errorCode) {
    return errorCode.getMessage();
  }
}