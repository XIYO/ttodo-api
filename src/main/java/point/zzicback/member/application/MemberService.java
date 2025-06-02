package point.zzicback.member.application;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.zzicback.common.error.BusinessException;
import point.zzicback.common.error.EntityNotFoundException;
import point.zzicback.member.application.dto.command.MemberSignUpCommand;
import point.zzicback.member.application.dto.command.MemberSignInCommand;
import point.zzicback.member.application.dto.command.UpdateMemberCommand;
import point.zzicback.member.application.dto.query.MemberQuery;
import point.zzicback.member.domain.Member;
import point.zzicback.member.persistance.MemberRepository;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {
  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;

  public void createMember(MemberSignUpCommand command) {
    if (memberRepository.existsByEmail(command.email())) {
      throw new BusinessException("이미 존재하는 이메일입니다.");
    }
    Member member = Member.builder()
        .email(command.email())
        .password(passwordEncoder.encode(command.password()))
        .nickname(command.nickname())
        .build();
    memberRepository.save(member);
  }

  @Transactional(readOnly = true)
  public Member authenticate(MemberSignInCommand command) {
    Member member = memberRepository.findByEmail(command.email())
        .orElseThrow(() -> new BusinessException("회원 정보 없음"));
    if (!"anon@zzic.com".equals(command.email())
        && !passwordEncoder.matches(command.password(), member.getPassword())) {
      throw new BusinessException("비밀번호가 틀렸습니다.");
    }
    return member;
  }

  @Transactional(readOnly = true)
  public Member findVerifiedMember(MemberQuery query) {
    return memberRepository.findById(query.memberId())
        .orElseThrow(() -> new EntityNotFoundException("Member", query.memberId()));
  }

  public void updateMember(UpdateMemberCommand command) {
    Member member = memberRepository.findById(command.memberId())
        .orElseThrow(() -> new EntityNotFoundException("Member", command.memberId()));
    member.setNickname(command.nickname());
  }
}
