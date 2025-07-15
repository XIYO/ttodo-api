package point.ttodoApi.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;

import java.time.Instant;

/**
 * API 응답을 위한 공통 DTO
 * 모든 API 응답을 일관된 형식으로 래핑
 * 
 * @param <T> 응답 데이터 타입
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "API 공통 응답")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    @Schema(description = "성공 여부", example = "true")
    private boolean success;
    
    @Schema(description = "응답 코드", example = "200")
    private int code;
    
    @Schema(description = "응답 메시지", example = "요청이 성공적으로 처리되었습니다.")
    private String message;
    
    @Schema(description = "응답 데이터")
    private T data;
    
    @Schema(description = "에러 상세 정보", implementation = ErrorDetail.class)
    private ErrorDetail error;
    
    @Schema(description = "응답 시간", example = "2024-01-01T00:00:00Z")
    @Builder.Default
    private Instant timestamp = Instant.now();
    
    /**
     * 성공 응답 생성
     * 
     * @param data 응답 데이터
     * @param <T> 데이터 타입
     * @return ApiResponse 객체
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .code(HttpStatus.OK.value())
                .message("요청이 성공적으로 처리되었습니다.")
                .data(data)
                .build();
    }
    
    /**
     * 성공 응답 생성 (메시지 포함)
     * 
     * @param data 응답 데이터
     * @param message 응답 메시지
     * @param <T> 데이터 타입
     * @return ApiResponse 객체
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .code(HttpStatus.OK.value())
                .message(message)
                .data(data)
                .build();
    }
    
    /**
     * 성공 응답 생성 (데이터 없음)
     * 
     * @param message 응답 메시지
     * @return ApiResponse 객체
     */
    public static ApiResponse<Void> success(String message) {
        return ApiResponse.<Void>builder()
                .success(true)
                .code(HttpStatus.OK.value())
                .message(message)
                .build();
    }
    
    /**
     * 생성 성공 응답
     * 
     * @param data 생성된 데이터
     * @param <T> 데이터 타입
     * @return ApiResponse 객체
     */
    public static <T> ApiResponse<T> created(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .code(HttpStatus.CREATED.value())
                .message("리소스가 성공적으로 생성되었습니다.")
                .data(data)
                .build();
    }
    
    /**
     * 실패 응답 생성
     * 
     * @param status HTTP 상태
     * @param message 에러 메시지
     * @return ApiResponse 객체
     */
    public static ApiResponse<Void> error(HttpStatus status, String message) {
        return ApiResponse.<Void>builder()
                .success(false)
                .code(status.value())
                .message(message)
                .build();
    }
    
    /**
     * 실패 응답 생성 (에러 상세 정보 포함)
     * 
     * @param status HTTP 상태
     * @param message 에러 메시지
     * @param errorDetail 에러 상세 정보
     * @return ApiResponse 객체
     */
    public static ApiResponse<Void> error(HttpStatus status, String message, ErrorDetail errorDetail) {
        return ApiResponse.<Void>builder()
                .success(false)
                .code(status.value())
                .message(message)
                .error(errorDetail)
                .build();
    }
    
    /**
     * 에러 상세 정보
     */
    @Getter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "에러 상세 정보")
    public static class ErrorDetail {
        
        @Schema(description = "에러 코드", example = "INVALID_REQUEST")
        private String errorCode;
        
        @Schema(description = "에러 상세 메시지", example = "요청 데이터가 유효하지 않습니다.")
        private String detail;
        
        @Schema(description = "필드별 에러 정보", example = "{\"email\": \"이메일 형식이 올바르지 않습니다.\"}")
        private Object fieldErrors;
        
        @Schema(description = "추적 ID", example = "550e8400-e29b-41d4-a716-446655440000")
        private String traceId;
    }
}