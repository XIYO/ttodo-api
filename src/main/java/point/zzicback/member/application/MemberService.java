package point.zzicback.member.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.zzicback.common.error.*;
import point.zzicback.member.application.dto.command.*;
import point.zzicback.member.domain.*;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {
  private final MemberRepository memberRepository;

  public void createMember(CreateMemberCommand command) {
    Member member = Member.builder()
        .email(command.email())
        .password(command.password())
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
