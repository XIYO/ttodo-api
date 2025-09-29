package point.ttodoApi.auth.infrastructure.jwt;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * JWT를 Spring Security Authentication으로 변환
 * 표준 User 객체만 사용
 */
@Component
public class CustomJwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {
    
    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        // JWT subject에서 UUID 추출
        String userId = jwt.getSubject();
        
        // 권한 정보 추출 (scope claim 사용)
        String scopeString = jwt.getClaimAsString("scope");
        List<SimpleGrantedAuthority> authorities = scopeString == null
                ? List.of(new SimpleGrantedAuthority("ROLE_USER"))
                : Arrays.stream(scopeString.split(" "))
                    .map(SimpleGrantedAuthority::new)
                    .toList();
        
        // Spring Security의 표준 User 객체 생성
        // UUID를 username으로 사용
        User user = new User(
            userId,        // username = UUID
            "",           // password (JWT 인증이므로 빈 문자열)
            true,         // enabled
            true,         // accountNonExpired
            true,         // credentialsNonExpired
            true,         // accountNonLocked
            authorities   // 권한 목록
        );
        
        // JWT를 credentials로 보관 (필요시 추가 정보 접근 가능)
        return new UsernamePasswordAuthenticationToken(user, jwt, authorities);
    }
}
