package point.ttodoApi.shared.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import point.ttodoApi.member.domain.Member;
import point.ttodoApi.member.infrastructure.persistence.MemberRepository;

import java.util.*;

/**
 * Spring Security 컨텍스트에서 현재 사용자 정보를 제공하는 서비스
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityService {
    
    private final MemberRepository memberRepository;
    
    /**
     * 현재 로그인한 멤버 ID 조회
     * @return 로그인한 멤버 ID (Optional)
     */
    public Optional<UUID> getCurrentMemberId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || 
            !authentication.isAuthenticated() || 
            authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        
        try {
            // Principal이 MemberPrincipal 타입인 경우
            if (authentication.getPrincipal() instanceof MemberPrincipal) {
                MemberPrincipal principal = (MemberPrincipal) authentication.getPrincipal();
                return Optional.of(principal.getMemberId());
            }
            
            // Principal이 String 타입인 경우 (테스트나 특수한 경우)
            if (authentication.getPrincipal() instanceof String) {
                String principalStr = (String) authentication.getPrincipal();
                if (!"anonymousUser".equals(principalStr)) {
                    try {
                        UUID memberId = UUID.fromString(principalStr);
                        return Optional.of(memberId);
                    } catch (IllegalArgumentException e) {
                        log.warn("Invalid UUID format in principal: {}", principalStr);
                    }
                }
            }
            
            log.debug("Unsupported principal type: {}", authentication.getPrincipal().getClass());
            return Optional.empty();
            
        } catch (Exception e) {
            log.error("Error getting current member ID", e);
            return Optional.empty();
        }
    }
    
    /**
     * 현재 로그인한 멤버 엔티티 조회
     * @return 로그인한 멤버 엔티티 (Optional)
     */
    public Optional<Member> getCurrentMember() {
        return getCurrentMemberId()
            .flatMap(memberRepository::findById);
    }
    
    /**
     * 현재 사용자가 인증되었는지 확인
     * @return 인증 여부
     */
    public boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        return authentication != null && 
               authentication.isAuthenticated() && 
               !(authentication instanceof AnonymousAuthenticationToken);
    }
    
    /**
     * 현재 사용자가 특정 권한을 가지고 있는지 확인
     * @param role 확인할 권한 (예: "ROLE_USER", "ROLE_ADMIN")
     * @return 권한 보유 여부
     */
    public boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        
        return authentication.getAuthorities().stream()
            .anyMatch(authority -> authority.getAuthority().equals(role));
    }
    
    /**
     * 현재 사용자가 ADMIN 권한을 가지고 있는지 확인
     * @return ADMIN 권한 보유 여부
     */
    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN");
    }
    
    /**
     * 현재 사용자가 특정 멤버와 동일한지 확인
     * @param memberId 비교할 멤버 ID
     * @return 동일 여부
     */
    public boolean isSameMember(UUID memberId) {
        if (memberId == null) return false;
        
        return getCurrentMemberId()
            .map(currentId -> currentId.equals(memberId))
            .orElse(false);
    }
    
    /**
     * 현재 사용자가 특정 멤버에 대한 접근 권한이 있는지 확인 (본인이거나 ADMIN)
     * @param memberId 접근하려는 멤버 ID
     * @return 접근 권한 보유 여부
     */
    public boolean canAccessMember(UUID memberId) {
        return isSameMember(memberId) || isAdmin();
    }
}

/**
 * Spring Security Principal 구현체
 * UserDetailsService에서 생성하여 사용
 */
@RequiredArgsConstructor
@lombok.Getter
class MemberPrincipal {
    private final UUID memberId;
    private final String email;
    private final String nickname;
}