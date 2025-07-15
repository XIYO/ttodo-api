package point.ttodoApi.common.error;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.HttpRequestMethodNotSupportedException;

import java.nio.file.AccessDeniedException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private final ErrorMetricsCollector metricsCollector;
    
    public GlobalExceptionHandler(ErrorMetricsCollector metricsCollector) {
        this.metricsCollector = metricsCollector;
    }
    
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex, HttpServletRequest request) {
        log.warn("Business exception occurred - errorCode: {}, status: {}, message: {}", 
                ex.getErrorCode(), ex.getHttpStatus(), ex.getMessage());
        
        metricsCollector.recordError(ex.getErrorCode(), ex.getHttpStatus().value(), ex.getClass().getSimpleName());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .type(buildErrorType(ex.getErrorCode()))
                .title(getErrorTitle(ex.getErrorCode()))
                .status(ex.getHttpStatus().value())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .errorCode(ex.getErrorCode())
                .timestamp(java.time.LocalDateTime.now())
                .build();
        
        return ResponseEntity.status(ex.getHttpStatus()).body(errorResponse);
    }
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, HttpServletRequest request) {
        log.warn("Business exception occurred - errorCode: {}, status: {}, message: {}", 
                ex.getErrorCodeValue(), ex.getHttpStatus(), ex.getMessage());
        
        metricsCollector.recordError(ex.getErrorCodeValue(), ex.getHttpStatus().value(), ex.getClass().getSimpleName());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .type(buildErrorType(ex.getErrorCode()))
                .title(ex.getErrorCode().getMessage())
                .status(ex.getHttpStatus().value())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .errorCode(ex.getErrorCodeValue())
                .timestamp(java.time.LocalDateTime.now())
                .build();
        
        return ResponseEntity.status(ex.getHttpStatus()).body(errorResponse);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        Map<String, String> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value",
                        (existing, replacement) -> existing
                ));
        
        fieldErrors.keySet().forEach(field -> metricsCollector.recordValidationError(field));
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .type("/errors/validation-error")
                .title("입력값 검증 실패")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("요청 데이터가 유효하지 않습니다.")
                .instance(request.getRequestURI())
                .errorCode(ErrorCode.VALIDATION_ERROR.getCode())
                .timestamp(java.time.LocalDateTime.now())
                .extensions(Map.of("errors", fieldErrors))
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameter(
            MissingServletRequestParameterException ex, HttpServletRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .type("/errors/missing-parameter")
                .title("필수 파라미터 누락")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(String.format("필수 파라미터 '%s'이(가) 누락되었습니다.", ex.getParameterName()))
                .instance(request.getRequestURI())
                .errorCode(ErrorCode.MISSING_PARAMETER.getCode())
                .timestamp(java.time.LocalDateTime.now())
                .extensions(Map.of(
                        "parameterName", ex.getParameterName(),
                        "parameterType", ex.getParameterType()
                ))
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    @ExceptionHandler(MissingServletRequestPartException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestPart(
            MissingServletRequestPartException ex, HttpServletRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .type("/errors/missing-file")
                .title("필수 파일 누락")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(String.format("필수 파일 파라미터 '%s'이(가) 누락되었습니다.", ex.getRequestPartName()))
                .instance(request.getRequestURI())
                .errorCode(ErrorCode.MISSING_FILE.getCode())
                .timestamp(java.time.LocalDateTime.now())
                .extensions(Map.of("requestPartName", ex.getRequestPartName()))
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatch(
            MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        
        String detail = String.format("파라미터 '%s'의 값 '%s'은(는) %s 타입으로 변환할 수 없습니다.",
                ex.getName(), ex.getValue(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "required");
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .type("/errors/invalid-argument")
                .title("잘못된 파라미터 타입")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail(detail)
                .instance(request.getRequestURI())
                .errorCode(ErrorCode.INVALID_ARGUMENT.getCode())
                .timestamp(java.time.LocalDateTime.now())
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupported(
            HttpRequestMethodNotSupportedException ex, HttpServletRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .type("/errors/method-not-allowed")
                .title("허용되지 않은 메소드")
                .status(HttpStatus.METHOD_NOT_ALLOWED.value())
                .detail(String.format("'%s' 메소드는 이 엔드포인트에서 지원되지 않습니다.", ex.getMethod()))
                .instance(request.getRequestURI())
                .errorCode(ErrorCode.METHOD_NOT_ALLOWED.getCode())
                .timestamp(java.time.LocalDateTime.now())
                .extensions(Map.of("supportedMethods", ex.getSupportedHttpMethods() != null ? ex.getSupportedHttpMethods() : Collections.emptySet()))
                .build();
        
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(errorResponse);
    }
    
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandlerFoundException(
            NoHandlerFoundException ex, HttpServletRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .type("/errors/resource-not-found")
                .title("엔드포인트를 찾을 수 없음")
                .status(HttpStatus.NOT_FOUND.value())
                .detail(String.format("요청한 URL '%s'에 해당하는 엔드포인트를 찾을 수 없습니다.", ex.getRequestURL()))
                .instance(request.getRequestURI())
                .errorCode(ErrorCode.RESOURCE_NOT_FOUND.getCode())
                .timestamp(java.time.LocalDateTime.now())
                .build();
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(
            MaxUploadSizeExceededException ex, HttpServletRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .type("/errors/file-size-exceeded")
                .title("파일 크기 초과")
                .status(HttpStatus.BAD_REQUEST.value())
                .detail("업로드 파일 크기가 허용된 최대 크기를 초과했습니다.")
                .instance(request.getRequestURI())
                .errorCode(ErrorCode.FILE_SIZE_EXCEEDED.getCode())
                .timestamp(java.time.LocalDateTime.now())
                .extensions(Map.of("maxSize", ex.getMaxUploadSize()))
                .build();
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex, HttpServletRequest request) {
        
        metricsCollector.recordAuthenticationFailure(ex.getClass().getSimpleName());
        
        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.AUTHENTICATION_FAILED,
                "인증이 필요합니다. 유효한 인증 정보를 제공해주세요.",
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex, HttpServletRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.ACCESS_DENIED,
                "해당 리소스에 접근할 권한이 없습니다.",
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }
    
    @ExceptionHandler(AuthorizationDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAuthorizationDeniedException(
            AuthorizationDeniedException ex, HttpServletRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.ACCESS_DENIED,
                "해당 리소스에 접근할 권한이 없습니다.",
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorResponse);
    }
    
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex, HttpServletRequest request) {
        
        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.INVALID_ARGUMENT,
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error occurred", ex);
        
        metricsCollector.recordError(ErrorCode.INTERNAL_SERVER_ERROR.getCode(), 
                                   HttpStatus.INTERNAL_SERVER_ERROR.value(), 
                                   ex.getClass().getSimpleName());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .type("/errors/internal-server-error")
                .title("서버 내부 오류")
                .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .detail("서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.")
                .instance(request.getRequestURI())
                .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                .timestamp(java.time.LocalDateTime.now())
                .build();
        
        if (log.isDebugEnabled()) {
            errorResponse = ErrorResponse.builder()
                    .type("/errors/internal-server-error")
                    .title("서버 내부 오류")
                    .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
                    .detail("서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.")
                    .instance(request.getRequestURI())
                    .errorCode(ErrorCode.INTERNAL_SERVER_ERROR.getCode())
                    .timestamp(java.time.LocalDateTime.now())
                    .extensions(Map.of(
                            "exceptionType", ex.getClass().getSimpleName(),
                            "exceptionMessage", ex.getMessage() != null ? ex.getMessage() : "No message"
                    ))
                    .build();
        }
        
        return ResponseEntity.internalServerError().body(errorResponse);
    }
    
    // Legacy exception handlers for backward compatibility
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEntityNotFound(EntityNotFoundException ex, HttpServletRequest request) {
        log.warn("Entity not found - message: {}", ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.of(
                ErrorCode.RESOURCE_NOT_FOUND,
                ex.getMessage(),
                request.getRequestURI()
        );
        
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }
    
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFoundException(NotFoundException ex, HttpServletRequest request) {
        log.warn("Resource not found - errorCode: {}, message: {}", ex.getErrorCodeValue(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .type(buildErrorType(ex.getErrorCode()))
                .title("리소스를 찾을 수 없음")
                .status(ex.getHttpStatus().value())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .errorCode(ex.getErrorCodeValue())
                .timestamp(java.time.LocalDateTime.now())
                .build();
        
        return ResponseEntity.status(ex.getHttpStatus()).body(errorResponse);
    }
    
    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflictException(ConflictException ex, HttpServletRequest request) {
        log.warn("Resource conflict - errorCode: {}, message: {}", ex.getErrorCodeValue(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .type(buildErrorType(ex.getErrorCode()))
                .title("리소스 충돌")
                .status(ex.getHttpStatus().value())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .errorCode(ex.getErrorCodeValue())
                .timestamp(java.time.LocalDateTime.now())
                .build();
        
        return ResponseEntity.status(ex.getHttpStatus()).body(errorResponse);
    }
    
    @ExceptionHandler(ForbiddenException.class)
    public ResponseEntity<ErrorResponse> handleForbiddenException(ForbiddenException ex, HttpServletRequest request) {
        log.warn("Access forbidden - errorCode: {}, message: {}", ex.getErrorCodeValue(), ex.getMessage());
        
        ErrorResponse errorResponse = ErrorResponse.builder()
                .type(buildErrorType(ex.getErrorCode()))
                .title("접근 권한 없음")
                .status(ex.getHttpStatus().value())
                .detail(ex.getMessage())
                .instance(request.getRequestURI())
                .errorCode(ex.getErrorCodeValue())
                .timestamp(java.time.LocalDateTime.now())
                .build();
        
        return ResponseEntity.status(ex.getHttpStatus()).body(errorResponse);
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