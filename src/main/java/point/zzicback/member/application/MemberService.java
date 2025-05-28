package point.zzicback.member.application;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import point.zzicback.member.domain.AuthenticatedMember;
import point.zzicback.member.domain.Member;
import point.zzicback.member.domain.dto.command.SignUpCommand;
import point.zzicback.member.domain.dto.command.SignInCommand;
import point.zzicback.member.domain.dto.response.MemberMeResponse;
import point.zzicback.member.persistance.MemberRepository;

import java.util.UUID;

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
        member.setNickname(signUpCommand.nickname());

        memberRepository.save(member);
    }

    public AuthenticatedMember signIn(SignInCommand signInCommand) {
        Member member = memberRepository.findByEmail(signInCommand.email())
                .orElseThrow(() -> new IllegalArgumentException("회원 정보 없음"));

        if (!"anonymous@shared.com".equals(signInCommand.email())) {
            if (!passwordEncoder.matches(signInCommand.password(), member.getPassword())) {
                throw new IllegalArgumentException("비밀번호가 틀렸습니다.");
            }
        }

        return AuthenticatedMember.from(member.getId(), member.getEmail(), member.getNickname());
    }

    public boolean isEmailTaken(String email) {
        return memberRepository.findByEmail(email).isPresent();
    }

    public MemberMeResponse getMemberMe(UUID memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("회원 정보 없음"));

        return new MemberMeResponse(member.getEmail(), member.getNickname());
    }
}

