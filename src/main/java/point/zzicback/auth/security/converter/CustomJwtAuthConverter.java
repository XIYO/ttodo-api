package point.zzicback.auth.security.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import point.zzicback.auth.security.principal.MemberPrincipal;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
public class CustomJwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        UUID id = UUID.fromString(jwt.getSubject());
        String email = jwt.getClaimAsString("email");
        String nickname = jwt.getClaimAsString("nickname");
        String scopeString = jwt.getClaimAsString("scope");
        List<SimpleGrantedAuthority> authorities = scopeString == null
                ? Collections.emptyList()
                : Arrays.stream(scopeString.split(" ")).map(SimpleGrantedAuthority::new).toList();

        MemberPrincipal principal = new MemberPrincipal(id, email, nickname, authorities);
        return new UsernamePasswordAuthenticationToken(principal, jwt, authorities);
    }
}
