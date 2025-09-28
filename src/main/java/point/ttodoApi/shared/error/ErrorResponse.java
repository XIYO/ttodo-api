package point.ttodoApi.shared.error;

import com.fasterxml.jackson.annotation.*;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "RFC 7807 Problem Details 표준을 따르는 에러 응답")
public class ErrorResponse {

  @Schema(description = "에러 유형을 나타내는 URI", example = "/errors/user-not-found")
  private final String type;

  @Schema(description = "에러 제목", example = "회원 정보를 찾을 수 없음")
  private final String title;

  @Schema(description = "HTTP 상태 코드", example = "404")
  private final int status;

  @Schema(description = "에러 상세 설명", example = "ID가 123인 회원을 찾을 수 없습니다.")
  private final String detail;

  @Schema(description = "에러가 발생한 인스턴스 URI", example = "/api/user/123")
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

  public static ErrorResponse ofValidation(Map<String, Object> fieldErrors) {
    return ErrorResponse.builder()
            .type("/errors/validation-failed")
            .title("입력값 검증 실패")
            .status(HttpStatus.BAD_REQUEST.value())
            .detail("입력된 데이터가 유효하지 않습니다.")
            .errorCode("VALIDATION_ERROR")
            .timestamp(LocalDateTime.now())
            .extensions(fieldErrors)
            .build();
  }

  public static ErrorResponse ofBadRequest(String detail) {
    return ErrorResponse.builder()
            .type("/errors/bad-request")
            .title("잘못된 요청")
            .status(HttpStatus.BAD_REQUEST.value())
            .detail(detail)
            .errorCode("BAD_REQUEST")
            .timestamp(LocalDateTime.now())
            .build();
  }

  public static ErrorResponse ofUnauthorized() {
    return ErrorResponse.builder()
            .type("/errors/unauthorized")
            .title("인증 필요")
            .status(HttpStatus.UNAUTHORIZED.value())
            .detail("인증이 필요한 서비스입니다.")
            .errorCode("UNAUTHORIZED")
            .timestamp(LocalDateTime.now())
            .build();
  }

  public static ErrorResponse ofForbidden() {
    return ErrorResponse.builder()
            .type("/errors/forbidden")
            .title("접근 권한 없음")
            .status(HttpStatus.FORBIDDEN.value())
            .detail("해당 리소스에 접근할 권한이 없습니다.")
            .errorCode("FORBIDDEN")
            .timestamp(LocalDateTime.now())
            .build();
  }

  public static ErrorResponse ofNotFound(String resourceType, String resourceId) {
    return ErrorResponse.builder()
            .type("/errors/not-found")
            .title(resourceType + "을(를) 찾을 수 없음")
            .status(HttpStatus.NOT_FOUND.value())
            .detail(String.format("%s(ID: %s)을(를) 찾을 수 없습니다.", resourceType, resourceId))
            .errorCode("NOT_FOUND")
            .timestamp(LocalDateTime.now())
            .build();
  }

  public static ErrorResponse ofConflict(String detail) {
    return ErrorResponse.builder()
            .type("/errors/conflict")
            .title("리소스 충돌")
            .status(HttpStatus.CONFLICT.value())
            .detail(detail)
            .errorCode("CONFLICT")
            .timestamp(LocalDateTime.now())
            .build();
  }

  public static ErrorResponse ofInternalServerError() {
    return ErrorResponse.builder()
            .type("/errors/internal-server-error")
            .title("서버 내부 오류")
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .detail("서버 처리 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.")
            .errorCode("INTERNAL_SERVER_ERROR")
            .timestamp(LocalDateTime.now())
            .build();
  }
}