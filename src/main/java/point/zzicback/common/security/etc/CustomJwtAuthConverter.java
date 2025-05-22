package point.zzicback.common.security.etc;

import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class CustomJwtAuthConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        /* 기본 클레임 추출 */
        String id = jwt.getClaimAsString("id"); // id 클레임에서 회원 ID 가져오기
        UUID memberId;
        try {
            // id가 null인 경우 예외를 던지기 전에 체크
            if (id == null) {
                memberId = UUID.randomUUID(); // id가 null인 경우 임시로 랜덤 UUID 생성
            } else {
                memberId = UUID.fromString(id);
            }
        } catch (IllegalArgumentException e) {
            // id가 UUID 형식이 아닌 경우
            memberId = UUID.randomUUID(); // 임시로 랜덤 UUID 생성
        }
        
        String email = jwt.getSubject(); // 이메일을 subject로 사용
        String nickname = jwt.getClaimAsString("nickname");
        
        // nickname이 null인 경우 기본값 설정
        if (nickname == null) {
            nickname = "익명 사용자";
        }

        /* 권한 → ROLE_ 접두어 없이 저장했다면 여기서 변환 */
        List<SimpleGrantedAuthority> authorities =
                jwt.getClaimAsStringList("roles") != null ?
                jwt.getClaimAsStringList("roles")
                        .stream()
                        .map(SimpleGrantedAuthority::new)
                        .toList() :
                List.of(new SimpleGrantedAuthority("ROLE_USER")); // 기본 권한 설정

        MemberPrincipal principal = new MemberPrincipal(memberId, email, nickname, authorities);

        /* credentials 자리에 원본 Jwt 를 넣어 두면 필요 시 꺼내 쓸 수 있음 */
        return new UsernamePasswordAuthenticationToken(principal, jwt, authorities);
    }
}