package point.zzicback.auth.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import point.zzicback.auth.application.dto.command.SignInCommand;
import point.zzicback.auth.application.dto.command.SignUpCommand;
import point.zzicback.auth.application.mapper.AuthApplicationMapper;
import point.zzicback.auth.domain.AuthenticatedMember;
import point.zzicback.member.domain.Member;
import point.zzicback.member.persistance.MemberRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    MemberRepository memberRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    AuthApplicationMapper authApplicationMapper;

    @InjectMocks
    AuthService authService;

    @Test
    @DisplayName("회원가입 성공")
    void signUp_success() {
        SignUpCommand command = new SignUpCommand("user@example.com", "1234SDFE@@#$", "닉네임");
        
        Member member = new Member();
        member.setEmail("user@example.com");
        member.setPassword("encodedPassword");
        member.setNickname("닉네임");

        when(memberRepository.existsByEmail(command.email())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(authApplicationMapper.toEntity(command)).thenReturn(member);

        authService.signUp(command);

        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(captor.capture());
        Member saved = captor.getValue();

        assertEquals("user@example.com", saved.getEmail());
        assertEquals("encodedPassword", saved.getPassword());
        assertEquals("닉네임", saved.getNickname());
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void signUp_fail_duplicateEmail() {
        SignUpCommand command = new SignUpCommand("user@example.com", "1234SDFE@@#$", "닉네임");

        when(memberRepository.existsByEmail(command.email())).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authService.signUp(command));
    }

    @Test
    @DisplayName("로그인 성공")
    void signIn_success() {
        SignInCommand command = new SignInCommand("user@example.com", "1234");

        UUID memberId = UUID.randomUUID();
        Member member = new Member();
        member.setId(memberId);
        member.setEmail("user@example.com");
        member.setPassword("encodedPassword");
        member.setNickname("테스트유저");

        when(memberRepository.findByEmail("user@example.com")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(command.password(), member.getPassword())).thenReturn(true);

        AuthenticatedMember result = authService.signIn(command);

        assertEquals(memberId.toString(), result.id());
        assertEquals("user@example.com", result.email());
        assertEquals("테스트유저", result.nickname());
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 틀림")
    void signIn_fail_wrongPassword() {
        SignInCommand command = new SignInCommand("user@example.com", "wrong");

        Member member = new Member();
        member.setEmail("user@example.com");
        member.setPassword("encodedPassword");

        when(memberRepository.findByEmail("user@example.com")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(command.password(), member.getPassword())).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> authService.signIn(command));
    }

    @Test
    @DisplayName("로그인 실패 - 사용자 없음")
    void signIn_fail_userNotFound() {
        SignInCommand command = new SignInCommand("notfound@example.com", "1234");

        when(memberRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> authService.signIn(command));
    }
}
