package point.zzicback.member.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import point.zzicback.common.utill.JwtUtil;
import point.zzicback.member.domain.AuthenticatedMember;
import point.zzicback.member.domain.Member;
import point.zzicback.member.application.dto.command.SignInCommand;
import point.zzicback.member.application.dto.command.SignUpCommand;
import point.zzicback.member.application.mapper.MemberApplicationMapper;
import point.zzicback.member.persistance.MemberRepository;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MemberServiceTest {

    @Mock
    MemberRepository memberRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @Mock
    JwtUtil jwtUtil;

    @Mock
    MemberApplicationMapper memberApplicationMapper;

    @InjectMocks
    MemberService memberService;

    @Test
    @DisplayName("회원가입 성공")
    void signUp_success() {
        // given
        SignUpCommand command = new SignUpCommand("user@example.com", "1234SDFE@@#$", "닉네임");
        
        Member member = new Member();
        member.setEmail("user@example.com");
        member.setPassword("encodedPassword");
        member.setNickname("닉네임");

        when(memberRepository.existsByEmail(command.email())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(memberApplicationMapper.toEntity(command)).thenReturn(member);

        // when
        memberService.signUp(command);

        // then
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
        // given
        SignUpCommand command = new SignUpCommand("user@example.com", "1234SDFE@@#$", "닉네임");

        when(memberRepository.existsByEmail(command.email())).thenReturn(true);

        // expect
        assertThrows(IllegalArgumentException.class, () -> memberService.signUp(command));
    }

    @Test
    @DisplayName("로그인 성공")
    void signIn_success() {
        // given
        SignInCommand command = new SignInCommand("user@example.com", "1234");

        UUID memberId = UUID.randomUUID();
        Member member = new Member();
        member.setId(memberId);
        member.setEmail("user@example.com");
        member.setPassword("encodedPassword");
        member.setNickname("테스트유저");

        when(memberRepository.findByEmail("user@example.com")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(command.password(), member.getPassword())).thenReturn(true);

        // when
        AuthenticatedMember result = memberService.signIn(command);

        // then
        assertEquals(memberId.toString(), result.id());
        assertEquals("user@example.com", result.email());
        assertEquals("테스트유저", result.nickname());
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 틀림")
    void signIn_fail_wrongPassword() {
        // given
        SignInCommand command = new SignInCommand("user@example.com", "wrong");

        Member member = new Member();
        member.setEmail("user@example.com");
        member.setPassword("encodedPassword");

        when(memberRepository.findByEmail("user@example.com")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(command.password(), member.getPassword())).thenReturn(false);

        // expect
        assertThrows(IllegalArgumentException.class, () -> memberService.signIn(command));
    }

    @Test
    @DisplayName("로그인 실패 - 사용자 없음")
    void signIn_fail_userNotFound() {
        // given
        SignInCommand command = new SignInCommand("notfound@example.com", "1234");

        when(memberRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        // expect
        assertThrows(IllegalArgumentException.class, () -> memberService.signIn(command));
    }
}
