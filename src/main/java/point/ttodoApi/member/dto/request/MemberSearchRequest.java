package point.ttodoApi.member.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.*;
import lombok.experimental.SuperBuilder;
import point.ttodoApi.common.dto.BaseSearchRequest;
import point.ttodoApi.member.domain.Role;

import java.time.LocalDateTime;

/**
 * Member 검색 요청 DTO
 */
@Getter
@Setter
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원 검색 조건")
public class MemberSearchRequest extends BaseSearchRequest {
    
    @Schema(description = "이메일 검색 키워드", example = "@example.com")
    @Size(max = 100, message = "이메일 검색어는 100자를 초과할 수 없습니다")
    private String emailKeyword;
    
    @Schema(description = "닉네임 검색 키워드", example = "홍길동")
    @Size(max = 50, message = "닉네임 검색어는 50자를 초과할 수 없습니다")
    private String nicknameKeyword;
    
    @Schema(description = "회원 권한", example = "USER")
    private Role role;
    
    @Schema(description = "마지막 로그인 시작 일시", example = "2024-01-01T00:00:00")
    private LocalDateTime lastLoginFrom;
    
    @Schema(description = "마지막 로그인 종료 일시", example = "2024-12-31T23:59:59")
    private LocalDateTime lastLoginTo;
    
    @Schema(description = "최근 활동 사용자만 조회 (30일 이내)", example = "false")
    private boolean recentlyActiveOnly;
    
    @Schema(description = "활성 상태", example = "true")
    private Boolean active;
    
    @Schema(description = "이메일 인증 여부", example = "true")
    private Boolean emailVerified;
    
    @Override
    public String getDefaultSort() {
        return "createdAt,desc";
    }
    
    @Override
    protected void validateBusinessRules() {
        // 로그인 날짜 범위 검증
        if (lastLoginFrom != null && lastLoginTo != null && lastLoginTo.isBefore(lastLoginFrom)) {
            throw new IllegalArgumentException("종료 일시는 시작 일시 이후여야 합니다");
        }
        
        // 최근 활동 사용자 조회 시 날짜 자동 설정
        if (recentlyActiveOnly) {
            lastLoginFrom = LocalDateTime.now().minusDays(30);
            lastLoginTo = LocalDateTime.now();
        }
    }
    
    /**
     * 검색 키워드가 있는지 확인
     */
    public boolean hasSearchKeyword() {
        return hasEmailKeyword() || hasNicknameKeyword();
    }
    
    /**
     * 이메일 키워드가 있는지 확인
     */
    public boolean hasEmailKeyword() {
        return emailKeyword != null && !emailKeyword.trim().isEmpty();
    }
    
    /**
     * 닉네임 키워드가 있는지 확인
     */
    public boolean hasNicknameKeyword() {
        return nicknameKeyword != null && !nicknameKeyword.trim().isEmpty();
    }
    
    /**
     * 로그인 날짜 필터가 있는지 확인
     */
    public boolean hasLoginDateFilter() {
        return lastLoginFrom != null || lastLoginTo != null;
    }
    
    /**
     * 권한 필터가 있는지 확인
     */
    public boolean hasRoleFilter() {
        return role != null;
    }
}