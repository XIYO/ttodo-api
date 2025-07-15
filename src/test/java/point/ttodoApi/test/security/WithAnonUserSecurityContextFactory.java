package point.ttodoApi.test.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.*;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.stereotype.Component;
import point.ttodoApi.auth.domain.MemberPrincipal;
import point.ttodoApi.member.domain.Member;
import point.ttodoApi.member.infrastructure.persistence.MemberRepository;

import java.util.*;

/**
 * WithAnonUser 애너테이션을 처리하는 팩토리
 * anon@ttodo.dev 사용자의 실제 정보를 사용하여 SecurityContext를 생성
 */
@Component
public class WithAnonUserSecurityContextFactory 
        implements WithSecurityContextFactory<WithAnonUser> {
    
    @Autowired
    private MemberRepository memberRepository;
    
    @Override
    public SecurityContext createSecurityContext(WithAnonUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        
        // anon@ttodo.dev 사용자 조회
        Member anonMember = memberRepository.findByEmail("anon@ttodo.dev")
                .orElseThrow(() -> new IllegalStateException("anon@ttodo.dev user not found in test"));
        
        List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        
        MemberPrincipal principal = MemberPrincipal.from(
                anonMember.getId(),
                anonMember.getEmail(),
                anonMember.getNickname(),
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