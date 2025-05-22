package point.zzicback.member.application;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import point.zzicback.common.utill.JwtUtil;
import point.zzicback.member.domain.Member;
import point.zzicback.member.domain.AuthenticatedMember;
import point.zzicback.member.domain.SignUpCommand;
import point.zzicback.member.domain.dto.SignInCommand;
import point.zzicback.member.domain.dto.request.SignUpRequest;
import point.zzicback.member.persistance.MemberRepository;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;

    public void signUp(SignUpCommand signUpCommand) {
        if (memberRepository.existsByEmail(signUpCommand.email())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다.");
        }
        Member member = new Member();
        member.setEmail(signUpCommand.email());
        member.setPassword(passwordEncoder.encode(signUpCommand.password()));
        member.setNickname(signUpCommand.nickName());

        memberRepository.save(member);
    }

    public AuthenticatedMember signIn(SignInCommand signInCommand) {
        Member member = memberRepository.findByEmail(signInCommand.email())
                .orElseThrow(() -> new IllegalArgumentException("회원 정보 없음"));

        if (!passwordEncoder.matches(signInCommand.password(), member.getPassword())) {
            throw new IllegalArgumentException("비밀번호가 틀렸습니다.");
        }

        return AuthenticatedMember.from(member.getId(), member.getEmail(), member.getNickname());
    }

    public boolean isEmailTaken(String email) {
        return memberRepository.findByEmail(email).isPresent();
    }
}

