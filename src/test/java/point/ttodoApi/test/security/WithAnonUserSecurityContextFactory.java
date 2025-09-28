package point.ttodoApi.test.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import point.ttodoApi.shared.security.UserPrincipal;

import java.util.List;
import java.util.UUID;

/**
 * Security context factory for WithAnonUser annotation
 */
public class WithAnonUserSecurityContextFactory implements WithSecurityContextFactory<WithAnonUser> {

    /**
     * Anonymous user UUID (consistent across tests)
     */
    private static final String ANON_USER_ID = "ffffffff-ffff-ffff-ffff-ffffffffffff";

    /**
     * Anonymous user email
     */
    private static final String ANON_USER_EMAIL = "anon@ttodo.dev";

    @Override
    public SecurityContext createSecurityContext(WithAnonUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        // Create UserPrincipal for anonymous user
        UserPrincipal userPrincipal = new UserPrincipal(
            UUID.fromString(ANON_USER_ID),
            ANON_USER_EMAIL,
            annotation.nickname(),
            annotation.timeZone(),
            annotation.locale(),
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
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