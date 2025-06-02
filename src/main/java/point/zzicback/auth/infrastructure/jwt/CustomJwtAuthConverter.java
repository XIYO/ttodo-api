package point.zzicback.auth.infrastructure.jwt;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import point.zzicback.auth.domain.MemberPrincipal;

import java.util.*;

@Component
public class CustomJwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {
  @Override
  public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
    UUID id = UUID.fromString(jwt.getSubject());
    String email = jwt.getClaimAsString("email");
    String nickname = jwt.getClaimAsString("nickname");
    String scopeString = jwt.getClaimAsString("scope");
    List<SimpleGrantedAuthority> authorities = scopeString == null
            ? Collections.emptyList()
            : Arrays.stream(scopeString.split(" ")).map(SimpleGrantedAuthority::new).toList();
    MemberPrincipal principal = MemberPrincipal.from(id, email, nickname, authorities);
    return new UsernamePasswordAuthenticationToken(principal, jwt, authorities);
  }
}
