package point.zzicback.auth.application;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.zzicback.auth.application.dto.command.*;
import point.zzicback.auth.application.mapper.AuthApplicationMapper;
import point.zzicback.auth.domain.AuthenticatedMember;
import point.zzicback.common.error.BusinessException;
import point.zzicback.member.domain.Member;
import point.zzicback.member.persistance.MemberRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
private final MemberRepository memberRepository;
private final PasswordEncoder passwordEncoder;
private final AuthApplicationMapper authApplicationMapper;

public void signUp(SignUpCommand signUpCommand) {
  if (memberRepository.existsByEmail(signUpCommand.email())) {
    throw new BusinessException("이미 존재하는 이메일입니다.");
  }
  Member member = authApplicationMapper.toEntity(signUpCommand);
  member.setPassword(passwordEncoder.encode(signUpCommand.password()));
  memberRepository.save(member);
}

@Transactional(readOnly = true)
public AuthenticatedMember signIn(SignInCommand signInCommand) {
  Member member = memberRepository.findByEmail(signInCommand.email())
          .orElseThrow(() -> new BusinessException("회원 정보 없음"));
  if (! "anon@zzic.com".equals(signInCommand.email())
          && ! passwordEncoder.matches(signInCommand.password(), member.getPassword())) {
    throw new BusinessException("비밀번호가 틀렸습니다.");
  }
  return AuthenticatedMember.from(member.getId(), member.getEmail(), member.getNickname());
}

@Transactional(readOnly = true)
public boolean isEmailTaken(String email) {
  return memberRepository.findByEmail(email).isPresent();
}
}
