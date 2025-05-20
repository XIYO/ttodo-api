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
import point.zzicback.member.domain.Member;
import point.zzicback.member.domain.dto.request.SignInRequest;
import point.zzicback.member.domain.dto.request.SignUpRequest;
import point.zzicback.member.persistance.MemberRepository;

import java.util.Optional;

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

    @InjectMocks
    MemberService memberService;

    @Test
    @DisplayName("회원가입 성공")
    void signUp_success() {
        // given
        SignUpRequest request = new SignUpRequest("user@example.com", "1234SDFE@@#$", "1234SDFE@@#$", "닉네임");

        when(memberRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");

        // when
        memberService.signUp(request);

        // then
        ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(captor.capture());
        Member saved = captor.getValue();

        assertEquals("user@example.com", saved.getEmail());
        assertEquals("encodedPassword", saved.getPassword());
        assertEquals("닉네임", saved.getNickName());
    }

    @Test
    @DisplayName("회원가입 실패 - 이메일 중복")
    void signUp_fail_duplicateEmail() {
        // given
        SignUpRequest request = new SignUpRequest("user@example.com", "1234SDFE@@#$", "1234SDFE@@#$", "닉네임");

        when(memberRepository.existsByEmail(request.email())).thenReturn(true);

        // expect
        assertThrows(IllegalArgumentException.class, () -> memberService.signUp(request));
    }

    @Test
    @DisplayName("로그인 성공")
    void signIn_success() {
        // given
        SignInRequest request = new SignInRequest("user@example.com", "1234");
        String expectedToken = "jwtToken";

        Member member = new Member();
        member.setEmail("user@example.com");
        member.setPassword("encodedPassword");

        when(memberRepository.findByEmail("user@example.com")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(request.password(), member.getPassword())).thenReturn(true);
        when(jwtUtil.generateJwtToken(member.getEmail())).thenReturn(expectedToken);

        // when
        String token = memberService.signIn(request);

        // then
        assertEquals(expectedToken, token);
    }

    @Test
    @DisplayName("로그인 실패 - 비밀번호 틀림")
    void signIn_fail_wrongPassword() {
        // given
        SignInRequest request = new SignInRequest("user@example.com", "wrong");

        Member member = new Member();
        member.setEmail("user@example.com");
        member.setPassword("encodedPassword");

        when(memberRepository.findByEmail("user@example.com")).thenReturn(Optional.of(member));
        when(passwordEncoder.matches(request.password(), member.getPassword())).thenReturn(false);

        // expect
        assertThrows(IllegalArgumentException.class, () -> memberService.signIn(request));
    }

    @Test
    @DisplayName("로그인 실패 - 사용자 없음")
    void signIn_fail_userNotFound() {
        // given
        SignInRequest request = new SignInRequest("notfound@example.com", "1234");

        when(memberRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        // expect
        assertThrows(IllegalArgumentException.class, () -> memberService.signIn(request));
    }
}
