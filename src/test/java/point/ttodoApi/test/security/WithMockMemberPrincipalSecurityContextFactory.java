package point.ttodoApi.test.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import point.ttodoApi.auth.domain.MemberPrincipal;

import java.util.*;
import java.util.stream.Collectors;

/**
 * WithMockMemberPrincipal 애너테이션을 처리하는 팩토리
 */
public class WithMockMemberPrincipalSecurityContextFactory 
        implements WithSecurityContextFactory<WithMockMemberPrincipal> {
    
    @Override
    public SecurityContext createSecurityContext(WithMockMemberPrincipal annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        
        UUID memberId = UUID.fromString(annotation.memberId());
        List<SimpleGrantedAuthority> authorities = Arrays.stream(annotation.roles())
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
        
        MemberPrincipal principal = MemberPrincipal.from(
                memberId,
                annotation.email(),
                annotation.nickname(),
                "Asia/Seoul",
                "ko_KR",
                authorities
        );
        
        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal, 
                null, 
                authorities
        );
        
        context.setAuthentication(auth);
        return context;
    }
}