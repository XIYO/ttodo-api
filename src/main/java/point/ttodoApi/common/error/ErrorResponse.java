package point.ttodoApi.common.error;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "RFC 7807 Problem Details 표준을 따르는 에러 응답")
public class ErrorResponse {
    
    @Schema(description = "에러 유형을 나타내는 URI", example = "/errors/member-not-found")
    private final String type;
    
    @Schema(description = "에러 제목", example = "회원 정보를 찾을 수 없음")
    private final String title;
    
    @Schema(description = "HTTP 상태 코드", example = "404")
    private final int status;
    
    @Schema(description = "에러 상세 설명", example = "ID가 123인 회원을 찾을 수 없습니다.")
    private final String detail;
    
    @Schema(description = "에러가 발생한 인스턴스 URI", example = "/api/members/123")
    private final String instance;
    
    @Schema(description = "애플리케이션 에러 코드", example = "MEMBER_001")
    private final String errorCode;
    
    @Schema(description = "에러 발생 시간", example = "2024-01-01T10:30:00")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private final LocalDateTime timestamp;
    
    @Schema(description = "추가 에러 정보")
    private final Map<String, Object> extensions;
    
    public static ErrorResponse of(ErrorCode errorCode) {
        return ErrorResponse.builder()
                .type("/errors/" + errorCode.name().toLowerCase().replace("_", "-"))
                .title(errorCode.getMessage())
                .status(errorCode.getHttpStatus().value())
                .errorCode(errorCode.getCode())
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static ErrorResponse of(ErrorCode errorCode, String detail) {
        return ErrorResponse.builder()
                .type("/errors/" + errorCode.name().toLowerCase().replace("_", "-"))
                .title(errorCode.getMessage())
                .status(errorCode.getHttpStatus().value())
                .detail(detail)
                .errorCode(errorCode.getCode())
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static ErrorResponse of(ErrorCode errorCode, String detail, String instance) {
        return ErrorResponse.builder()
                .type("/errors/" + errorCode.name().toLowerCase().replace("_", "-"))
                .title(errorCode.getMessage())
                .status(errorCode.getHttpStatus().value())
                .detail(detail)
                .instance(instance)
                .errorCode(errorCode.getCode())
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static ErrorResponse of(HttpStatus httpStatus, String title, String detail) {
        return ErrorResponse.builder()
                .type("/errors/generic")
                .title(title)
                .status(httpStatus.value())
                .detail(detail)
                .timestamp(LocalDateTime.now())
                .build();
    }
    
    public static ErrorResponseBuilder from(ErrorCode errorCode) {
        return ErrorResponse.builder()
                .type("/errors/" + errorCode.name().toLowerCase().replace("_", "-"))
                .title(errorCode.getMessage())
                .status(errorCode.getHttpStatus().value())
                .errorCode(errorCode.getCode())
                .timestamp(LocalDateTime.now());
    }
}