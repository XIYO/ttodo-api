package point.zzicback.member.application;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.zzicback.common.error.BusinessException;
import point.zzicback.common.error.EntityNotFoundException;
import point.zzicback.member.application.dto.command.CreateMemberCommand;
import point.zzicback.member.application.dto.command.UpdateMemberCommand;
import point.zzicback.member.domain.Member;
import point.zzicback.member.persistance.MemberRepository;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {
  private final MemberRepository memberRepository;
  private final PasswordEncoder passwordEncoder;

  public void createMember(CreateMemberCommand command) {
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
  public Member findByEmail(String email) {
    return memberRepository.findByEmail(email)
        .orElseThrow(() -> new BusinessException("회원 정보 없음"));
  }

  @Transactional(readOnly = true)
  public Member findVerifiedMember(UUID memberId) {
    return findMemberById(memberId);
  }

  public void updateMember(UpdateMemberCommand command) {
    Member member = findMemberById(command.memberId());
    member.setNickname(command.nickname());
  }

  private Member findMemberById(UUID memberId) {
    return memberRepository.findById(memberId)
        .orElseThrow(() -> new EntityNotFoundException("Member", memberId));
  }
}
