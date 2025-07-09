package point.ttodoApi.auth.infrastructure.jwt;

import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import point.ttodoApi.auth.domain.MemberPrincipal;

import java.util.*;

@Component
public class CustomJwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {
  @Override
  public AbstractAuthenticationToken convert(@NonNull Jwt jwt) {
    UUID id = UUID.fromString(jwt.getSubject());
    String email = jwt.getClaimAsString("email");
    String nickname = jwt.getClaimAsString("nickname");
    String timeZone = jwt.getClaimAsString("timeZone");
    String locale = jwt.getClaimAsString("locale");
    String scopeString = jwt.getClaimAsString("scope");
    List<SimpleGrantedAuthority> authorities = scopeString == null
            ? Collections.emptyList()
            : Arrays.stream(scopeString.split(" ")).map(SimpleGrantedAuthority::new).toList();
    MemberPrincipal principal = MemberPrincipal.from(id, email, nickname, timeZone, locale, authorities);
    return new UsernamePasswordAuthenticationToken(principal, jwt, authorities);
  }
}
