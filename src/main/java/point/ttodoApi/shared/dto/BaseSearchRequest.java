package point.ttodoApi.shared.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

/**
 * 검색 요청의 기본 클래스
 * 모든 검색 요청 DTO는 이 클래스를 상속받아 페이징과 정렬 기능을 제공받음
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "검색 요청 기본 정보")
public abstract class BaseSearchRequest {
    
    @Schema(description = "페이지 번호 (0부터 시작)", example = "0", defaultValue = "0")
    @Min(value = 0, message = "페이지 번호는 0 이상이어야 합니다")
    @Max(value = 10000, message = "페이지 번호는 10000을 초과할 수 없습니다")
    @Builder.Default
    private Integer page = 0;
    
    @Schema(description = "페이지 크기", example = "20", defaultValue = "20")
    @Min(value = 1, message = "페이지 크기는 1 이상이어야 합니다")
    @Max(value = 100, message = "페이지 크기는 100을 초과할 수 없습니다")
    @Builder.Default
    private Integer size = 20;
    
    @Schema(description = "정렬 조건 (필드명,방향 형식)", example = "createdAt,desc")
    @Pattern(
        regexp = "^([a-zA-Z][a-zA-Z0-9_]*(\\.[a-zA-Z][a-zA-Z0-9_]*)*,(asc|desc)(;|$))*$",
        message = "올바르지 않은 정렬 형식입니다. 예: 'field,asc' 또는 'field1,desc;field2,asc'"
    )
    private String sort;
    
    /**
     * 페이지 정보 유효성 검증
     * 하위 클래스에서 추가 검증이 필요한 경우 오버라이드
     */
    public void validate() {
        // 기본값 설정
        if (page == null) {
            page = 0;
        }
        if (size == null) {
            size = 20;
        }
        
        // 추가 비즈니스 규칙 검증
        validateBusinessRules();
    }
    
    /**
     * 비즈니스 규칙 검증
     * 하위 클래스에서 구현
     */
    protected void validateBusinessRules() {
        // 하위 클래스에서 필요시 구현
    }
    
    /**
     * 정렬 조건이 있는지 확인
     */
    public boolean hasSort() {
        return sort != null && !sort.trim().isEmpty();
    }
    
    /**
     * 기본 정렬 조건 설정
     * 하위 클래스에서 도메인별 기본 정렬을 정의
     */
    public abstract String defaultSort();
    
    /**
     * 최종 정렬 조건 반환
     * 사용자가 제공한 정렬이 없으면 기본 정렬 사용
     */
    public String finalSort() {
        return hasSort() ? sort : defaultSort();
    }
}