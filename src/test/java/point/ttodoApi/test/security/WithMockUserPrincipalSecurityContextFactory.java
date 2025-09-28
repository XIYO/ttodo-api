package point.ttodoApi.test.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import point.ttodoApi.shared.security.UserPrincipal;

import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Security context factory for WithMockUserPrincipal annotation
 */
public class WithMockUserPrincipalSecurityContextFactory implements WithSecurityContextFactory<WithMockUserPrincipal> {

    @Override
    public SecurityContext createSecurityContext(WithMockUserPrincipal annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        // Create UserPrincipal
        UserPrincipal userPrincipal = new UserPrincipal(
            UUID.fromString(annotation.userId()),
            annotation.email(),
            annotation.nickname(),
            annotation.timeZone(),
            annotation.locale(),
            Arrays.stream(annotation.roles())
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList())
        );

        // Create Authentication
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            userPrincipal,
            null,
            userPrincipal.getAuthorities()
        );

        context.setAuthentication(authentication);
        return context;
    }
}