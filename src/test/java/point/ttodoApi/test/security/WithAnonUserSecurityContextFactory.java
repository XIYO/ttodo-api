package point.ttodoApi.test.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;
import org.springframework.security.core.userdetails.User;

import java.util.List;

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

        // Create User for anonymous user
        User user = new User(
            ANON_USER_ID, // username as UUID string
            "", // no password for test
            true, true, true, true,
            List.of(new SimpleGrantedAuthority("ROLE_USER"))
        );

        // Create Authentication
        Authentication authentication = new UsernamePasswordAuthenticationToken(
            user,
            null,
            user.getAuthorities()
        );

        context.setAuthentication(authentication);
        return context;
    }
}