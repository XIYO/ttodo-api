package point.ttodoApi.common.error;

import lombok.*;
import org.springframework.http.HttpStatus;

/**
 * 검증 관련 오류 코드
 * 요청 파라미터 검증, 정렬 필드 검증 등에 사용
 */
@Getter
@RequiredArgsConstructor
public enum ValidationErrorCode {
    // 일반 검증 오류
    INVALID_PARAMETER("VALIDATION_001", "잘못된 파라미터", HttpStatus.BAD_REQUEST),
    INVALID_FORMAT("VALIDATION_002", "잘못된 형식", HttpStatus.BAD_REQUEST),
    
    // 정렬 관련 오류
    INVALID_SORT_FIELD("VALIDATION_003", "잘못된 정렬 필드", HttpStatus.BAD_REQUEST),
    INVALID_SORT_DIRECTION("VALIDATION_004", "잘못된 정렬 방향", HttpStatus.BAD_REQUEST),
    
    // 페이징 관련 오류
    INVALID_PAGE_NUMBER("VALIDATION_005", "잘못된 페이지 번호", HttpStatus.BAD_REQUEST),
    INVALID_PAGE_SIZE("VALIDATION_006", "잘못된 페이지 크기", HttpStatus.BAD_REQUEST),
    
    // 날짜 관련 오류
    INVALID_DATE_FORMAT("VALIDATION_007", "잘못된 날짜 형식", HttpStatus.BAD_REQUEST),
    INVALID_DATE_RANGE("VALIDATION_008", "잘못된 날짜 범위", HttpStatus.BAD_REQUEST),
    
    // 범위 관련 오류
    VALUE_OUT_OF_RANGE("VALIDATION_009", "값이 허용 범위를 벗어남", HttpStatus.BAD_REQUEST),
    STRING_TOO_LONG("VALIDATION_010", "문자열이 너무 깁니다", HttpStatus.BAD_REQUEST),
    STRING_TOO_SHORT("VALIDATION_011", "문자열이 너무 짧습니다", HttpStatus.BAD_REQUEST),
    
    // 패턴 관련 오류
    INVALID_PATTERN("VALIDATION_012", "잘못된 패턴", HttpStatus.BAD_REQUEST),
    INVALID_EMAIL_FORMAT("VALIDATION_013", "잘못된 이메일 형식", HttpStatus.BAD_REQUEST),
    INVALID_PHONE_FORMAT("VALIDATION_014", "잘못된 전화번호 형식", HttpStatus.BAD_REQUEST),
    
    // 엔티티 검증 오류
    CATEGORY_NOT_FOUND("VALIDATION_015", "카테고리를 찾을 수 없음", HttpStatus.NOT_FOUND),
    MEMBER_NOT_FOUND("VALIDATION_016", "회원을 찾을 수 없음", HttpStatus.NOT_FOUND),
    TODO_NOT_FOUND("VALIDATION_017", "할일을 찾을 수 없음", HttpStatus.NOT_FOUND),
    CHALLENGE_NOT_FOUND("VALIDATION_018", "챌린지를 찾을 수 없음", HttpStatus.NOT_FOUND),
    
    // 중복 검증 오류
    DUPLICATE_VALUE("VALIDATION_019", "중복된 값", HttpStatus.CONFLICT),
    ALREADY_EXISTS("VALIDATION_020", "이미 존재하는 값", HttpStatus.CONFLICT);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}