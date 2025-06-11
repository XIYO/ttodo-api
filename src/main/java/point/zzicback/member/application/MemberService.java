package point.zzicback.member.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.zzicback.common.error.*;
import point.zzicback.member.application.dto.command.*;
import point.zzicback.member.domain.*;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {
  private static final String MEMBER_ENTITY = "Member";
  private final MemberRepository memberRepository;

  public Member createMember(CreateMemberCommand command) {
    Member member = Member.builder()
        .email(command.email())
        .password(command.password())
        .nickname(command.nickname())
        .build();
    return memberRepository.save(member);
  }

  @Transactional(readOnly = true)
  public Optional<Member> findByEmail(String email) {
    return memberRepository.findByEmail(email);
  }

  @Transactional(readOnly = true)
  public Member findByEmailOrThrow(String email) {
    return memberRepository.findByEmail(email)
        .orElseThrow(() -> new BusinessException("회원 정보 없음"));
  }

  @Transactional(readOnly = true)
  public Optional<Member> findById(UUID memberId) {
    return memberRepository.findById(memberId);
  }

  @Transactional(readOnly = true)
  public Member findByIdOrThrow(UUID memberId) {
    return memberRepository.findById(memberId)
        .orElseThrow(() -> new EntityNotFoundException(MEMBER_ENTITY, memberId));
  }

  @Transactional(readOnly = true)
  public Member findVerifiedMember(UUID memberId) {
    return memberRepository.findById(memberId)
        .orElseThrow(() -> new EntityNotFoundException(MEMBER_ENTITY, memberId));
  }

  public void updateMember(UpdateMemberCommand command) {
    Member member = memberRepository.findById(command.memberId())
        .orElseThrow(() -> new EntityNotFoundException(MEMBER_ENTITY, command.memberId()));
    member.setNickname(command.nickname());
  }
}
