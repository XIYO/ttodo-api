package point.ttodoApi.shared.error;

import lombok.*;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
  // Common Errors
  VALIDATION_ERROR("COMMON_001", "입력값 검증 실패", HttpStatus.BAD_REQUEST),
  INVALID_ARGUMENT("COMMON_002", "잘못된 요청 파라미터", HttpStatus.BAD_REQUEST),
  INVALID_INPUT_VALUE("COMMON_008", "잘못된 입력값", HttpStatus.BAD_REQUEST),
  MISSING_PARAMETER("COMMON_003", "필수 파라미터 누락", HttpStatus.BAD_REQUEST),
  MISSING_FILE("COMMON_004", "필수 파일 누락", HttpStatus.BAD_REQUEST),
  RESOURCE_NOT_FOUND("COMMON_005", "리소스를 찾을 수 없음", HttpStatus.NOT_FOUND),
  INTERNAL_SERVER_ERROR("COMMON_006", "서버 내부 오류", HttpStatus.INTERNAL_SERVER_ERROR),
  METHOD_NOT_ALLOWED("COMMON_007", "허용되지 않은 메소드", HttpStatus.METHOD_NOT_ALLOWED),

  // Authentication & Authorization Errors
  AUTHENTICATION_FAILED("AUTH_001", "인증 실패", HttpStatus.UNAUTHORIZED),
  INVALID_TOKEN("AUTH_002", "유효하지 않은 토큰", HttpStatus.UNAUTHORIZED),
  TOKEN_EXPIRED("AUTH_003", "토큰이 만료됨", HttpStatus.UNAUTHORIZED),
  ACCESS_DENIED("AUTH_004", "접근 권한 없음", HttpStatus.FORBIDDEN),
  INVALID_CREDENTIALS("AUTH_005", "잘못된 인증 정보", HttpStatus.UNAUTHORIZED),

  // User Errors
  MEMBER_NOT_FOUND("MEMBER_001", "회원 정보를 찾을 수 없음", HttpStatus.NOT_FOUND),
  DUPLICATE_EMAIL("MEMBER_002", "이미 사용중인 이메일", HttpStatus.CONFLICT),
  INVALID_PASSWORD("MEMBER_003", "잘못된 비밀번호", HttpStatus.BAD_REQUEST),
  MEMBER_ALREADY_EXISTS("MEMBER_004", "이미 존재하는 회원", HttpStatus.CONFLICT),

  // Category Errors
  CATEGORY_NOT_FOUND("CATEGORY_001", "카테고리를 찾을 수 없음", HttpStatus.NOT_FOUND),
  DUPLICATE_CATEGORY_NAME("CATEGORY_002", "중복된 카테고리명", HttpStatus.CONFLICT),
  CATEGORY_IN_USE("CATEGORY_003", "사용중인 카테고리는 삭제할 수 없음", HttpStatus.CONFLICT),

  // Todo Errors
  TODO_NOT_FOUND("TODO_001", "할일을 찾을 수 없음", HttpStatus.NOT_FOUND),
  TODO_ALREADY_COMPLETED("TODO_002", "이미 완료된 할일", HttpStatus.CONFLICT),
  INVALID_TODO_DATE("TODO_003", "잘못된 할일 날짜", HttpStatus.BAD_REQUEST),
  INVALID_REPEAT_CONFIG("TODO_004", "잘못된 반복 설정", HttpStatus.BAD_REQUEST),

  // Challenge Errors
  CHALLENGE_NOT_FOUND("CHALLENGE_001", "챌린지를 찾을 수 없음", HttpStatus.NOT_FOUND),
  CHALLENGE_ALREADY_JOINED("CHALLENGE_002", "이미 참여중인 챌린지", HttpStatus.CONFLICT),
  CHALLENGE_FULL("CHALLENGE_003", "챌린지 참여 인원이 가득참", HttpStatus.CONFLICT),
  CHALLENGE_ENDED("CHALLENGE_004", "종료된 챌린지", HttpStatus.GONE),
  CHALLENGE_NOT_STARTED("CHALLENGE_005", "시작되지 않은 챌린지", HttpStatus.BAD_REQUEST),
  INVALID_INVITE_LINK("CHALLENGE_006", "유효하지 않은 초대 링크", HttpStatus.BAD_REQUEST),

  // File Upload Errors
  FILE_NOT_FOUND("FILE_001", "파일을 찾을 수 없음", HttpStatus.NOT_FOUND),
  INVALID_FILE_TYPE("FILE_002", "허용되지 않은 파일 형식", HttpStatus.BAD_REQUEST),
  FILE_SIZE_EXCEEDED("FILE_003", "파일 크기 초과", HttpStatus.BAD_REQUEST),
  FILE_UPLOAD_FAILED("FILE_004", "파일 업로드 실패", HttpStatus.INTERNAL_SERVER_ERROR),

  // Business Logic Errors
  INVALID_OPERATION("BIZ_001", "잘못된 작업 요청", HttpStatus.BAD_REQUEST),
  OPERATION_NOT_ALLOWED("BIZ_002", "허용되지 않은 작업", HttpStatus.FORBIDDEN),
  DEPENDENCY_EXISTS("BIZ_003", "종속성이 존재함", HttpStatus.CONFLICT),
  QUOTA_EXCEEDED("BIZ_004", "할당량 초과", HttpStatus.TOO_MANY_REQUESTS);

  private final String code;
  private final String message;
  private final HttpStatus httpStatus;
}