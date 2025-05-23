package point.zzicback.common.error;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, List<String>>> handleValidationError(MethodArgumentNotValidException ex) {
        Map<String, List<String>> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.groupingBy(
                        err -> err.getField(),
                        Collectors.mapping(err -> err.getDefaultMessage(), Collectors.toList())
                ));

        return ResponseEntity.badRequest().body(fieldErrors);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(
            IllegalArgumentException ex,
            HttpServletRequest request
    ) {
        ErrorResponse errorResponse = new ErrorResponse(
                ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toString(),
                HttpStatus.BAD_REQUEST.value(),
                "Bad Request",
                request.getRequestURI(),
                ex.getMessage(),
                null
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }
    
    @ExceptionHandler(JwtException.class)
    public ResponseEntity<ErrorResponse> handleJwtException(
            JwtException ex,
            HttpServletRequest request
    ) {
        ErrorResponse errorResponse = new ErrorResponse(
                ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toString(),
                HttpStatus.UNAUTHORIZED.value(),
                "Unauthorized",
                request.getRequestURI(),
                "JWT 토큰 처리 중 오류가 발생했습니다: " + ex.getMessage(),
                null
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }
    
    @ExceptionHandler(NullPointerException.class)
    public ResponseEntity<ErrorResponse> handleNullPointerException(
            NullPointerException ex,
            HttpServletRequest request
    ) {
        ErrorResponse errorResponse = new ErrorResponse(
                ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toString(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                request.getRequestURI(),
                "서버 내부 오류: " + ex.getMessage(),
                null
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    @ExceptionHandler(FieldValidationException.class)
    public ResponseEntity<Map<String, List<String>>> handleFieldValidation(FieldValidationException ex) {
        return ResponseEntity.badRequest().body(
                Map.of(ex.getField(), List.of(ex.getMessage()))
        );
    }
}