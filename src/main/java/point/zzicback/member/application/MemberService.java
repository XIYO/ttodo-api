package point.zzicback.member.application;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.zzicback.common.error.*;
import point.zzicback.member.application.dto.command.*;
import point.zzicback.member.application.dto.result.MemberResult;
import point.zzicback.member.application.event.MemberCreatedEvent;
import point.zzicback.member.domain.Member;
import point.zzicback.member.infrastructure.persistence.MemberRepository;
import point.zzicback.profile.application.ProfileService;
import point.zzicback.profile.domain.Profile;

import java.util.*;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {
  private static final String MEMBER_ENTITY = "Member";
  private final MemberRepository memberRepository;
  private final ProfileService profileService;
  private final ApplicationEventPublisher eventPublisher;

  public Member createMember(CreateMemberCommand command) {
    Member member = Member.builder()
        .email(command.email())
        .password(command.password())
        .nickname(command.nickname())
        .build();
    Member savedMember = memberRepository.save(member);
    
    // Create profile for the new member with introduction if provided
    Profile profile = profileService.createProfile(savedMember.getId());
    if (command.introduction() != null && !command.introduction().isEmpty()) {
      profile.updateIntroduction(command.introduction());
      profileService.saveProfile(profile);
    }
    
    eventPublisher.publishEvent(new MemberCreatedEvent(
        savedMember.getId(), 
        savedMember.getEmail(), 
        savedMember.getNickname()
    ));
    
    return savedMember;
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
    
    if (command.hasNickname()) {
      member.setNickname(command.nickname());
    }
    // introduction은 더 이상 Member에서 관리하지 않음 (Profile로 이동)
  }

  @Transactional(readOnly = true)
  public Page<MemberResult> getMembers(Pageable pageable) {
    return memberRepository.findAll(pageable)
            .map(member -> new MemberResult(
                    member.getId(),
                    member.getEmail(),
                    member.getNickname()));
  }

  @Transactional(readOnly = true)
  public MemberResult getMember(UUID memberId) {
    var member = findByIdOrThrow(memberId);
    return new MemberResult(
            member.getId(),
            member.getEmail(),
            member.getNickname());
  }
}
