package point.zzicback.auth.domain;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import point.zzicback.member.domain.Member;

import java.util.Collection;
import java.util.UUID;

public record MemberPrincipal(
        @Schema(description = "회원 고유 ID", example = "b1a2c3d4-e5f6-7890-1234-56789abcdef0") UUID id,
        @Schema(description = "이메일", example = "user@example.com") String email,
        @Schema(description = "닉네임", example = "홍길동") String nickname,
        Collection<? extends GrantedAuthority> authorities) implements UserDetails {

    public static MemberPrincipal from(Member member, Collection<? extends GrantedAuthority> authorities) {
        return new MemberPrincipal(member.getId(), member.getEmail(), member.getNickname(), authorities);
    }

    public static MemberPrincipal from(UUID id, String email, String nickname, Collection<? extends GrantedAuthority> authorities) {
        return new MemberPrincipal(id, email, nickname, authorities);
    }

    public String idAsString() {
        return id.toString();
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
