package point.ttodoApi.challenge.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.ttodoApi.challenge.application.mapper.ChallengeMapper;
import point.ttodoApi.challenge.application.result.ParticipantResult;
import point.ttodoApi.challenge.domain.*;
import point.ttodoApi.challenge.infrastructure.*;
import point.ttodoApi.user.domain.User;
import point.ttodoApi.shared.error.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class ChallengeParticipationService {
  private final ChallengeParticipationRepository participationRepository;
  private final ChallengeRepository challengeRepository;
  private final ChallengeMapper challengeMapper;

  // 공개 챌린지 참여
  public ChallengeParticipation joinChallenge(Long challengeId, User user) {
    log.debug("공개 챌린지 참여 시도 - challengeId: {}, userId: {}", challengeId, user.getId());

    Challenge challenge = challengeRepository.findById(challengeId)
            .orElseThrow(() -> {
              log.warn("챌린지를 찾을 수 없음 - challengeId: {}", challengeId);
              return new NotFoundException("챌린지를 찾을 수 없습니다");
            });

    log.debug("챌린지 조회 성공 - challengeId: {}, visibility: {}", challengeId, challenge.getVisibility());

    // 초대 전용 챌린지는 직접 참여 불가
    if (challenge.getVisibility() == ChallengeVisibility.INVITE_ONLY) {
      log.warn("초대 전용 챌린지 직접 참여 시도 - challengeId: {}, userId: {}", challengeId, user.getId());
      throw new ForbiddenException("초대 링크를 통해서만 참여할 수 있습니다");
    }

    return joinChallengeInternal(challenge, user);
  }

  // 초대 코드로 참여
  public void joinByInviteCode(String inviteCode, User user) {
    log.debug("초대 코드로 참여 시도 - inviteCode: {}, userId: {}", inviteCode, user.getId());

    Challenge challenge = challengeRepository.findByInviteCode(inviteCode)
            .orElseThrow(() -> {
              log.warn("유효하지 않은 초대 코드 - inviteCode: {}", inviteCode);
              return new NotFoundException("유효하지 않은 초대 코드입니다");
            });

    log.debug("초대 코드로 챌린지 조회 성공 - challengeId: {}, inviteCode: {}", challenge.getId(), inviteCode);
    joinChallengeInternal(challenge, user);
  }

  // 공통 참여 로직
  private ChallengeParticipation joinChallengeInternal(Challenge challenge, User user) {
    log.debug("챌린지 참여 내부 로직 시작 - challengeId: {}, userId: {}", challenge.getId(), user.getId());

    try {
      // 활성 상태 확인
      boolean isActive = challenge.isActive();
      log.debug("챌린지 활성 상태 확인 - challengeId: {}, isActive: {}", challenge.getId(), isActive);
      if (!isActive) {
        log.warn("비활성 챌린지 참여 시도 - challengeId: {}", challenge.getId());
        throw new BusinessException(ErrorCode.INVALID_OPERATION, "종료되었거나 시작되지 않은 챌린지입니다");
      }

      // 참여 가능 여부 확인
      boolean isJoinable = challenge.isJoinable();
      log.debug("챌린지 참여 가능 여부 확인 - challengeId: {}, isJoinable: {}", challenge.getId(), isJoinable);
      if (!isJoinable) {
        log.warn("참여 불가능한 챌린지 참여 시도 - challengeId: {}", challenge.getId());
        throw new BusinessException(ErrorCode.CHALLENGE_FULL, "참여 인원이 가득 찼습니다");
      }

      // 중복 참여 확인
      boolean alreadyParticipating = participationRepository.existsByChallenge_IdAndUser_IdAndJoinOutIsNull(
              challenge.getId(), user.getId());
      log.debug("중복 참여 확인 - challengeId: {}, userId: {}, alreadyParticipating: {}",
              challenge.getId(), user.getId(), alreadyParticipating);
      if (alreadyParticipating) {
        log.warn("중복 참여 시도 - challengeId: {}, userId: {}", challenge.getId(), user.getId());
        throw new ConflictException("이미 참여 중인 챌린지입니다");
      }

      ChallengeParticipation participation = ChallengeParticipation.builder()
              .challenge(challenge)
              .user(user)
              .joinedAt(LocalDateTime.now())
              .build();

      log.debug("챌린지 참여 엔티티 생성 완료 - challengeId: {}, userId: {}", challenge.getId(), user.getId());
      ChallengeParticipation saved = participationRepository.save(participation);
      log.info("챌린지 참여 성공 - challengeId: {}, userId: {}, participationId: {}",
              challenge.getId(), user.getId(), saved.getId());

      return saved;
    } catch (Exception e) {
      log.error("챌린지 참여 중 오류 발생 - challengeId: {}, userId: {}, error: {}",
              challenge.getId(), user.getId(), e.getMessage(), e);
      throw e;
    }
  }

  // 중도탈퇴 (soft delete)
  public void leaveChallenge(Long challengeId, User user) {
    ChallengeParticipation participation = participationRepository
            .findByUserAndChallenge_IdAndJoinOutIsNull(user, challengeId)
            .orElseThrow(() -> new NotFoundException("참여하지 않은 챌린지입니다"));

    // 이미 탈퇴한 경우
    if (participation.getJoinOut() != null) throw new ConflictException("이미 탈퇴한 챌린지입니다");

    participation.setJoinOut(LocalDateTime.now());
    participationRepository.save(participation);
  }

  /**
   * 특정 챌린지의 참여자 목록을 Application DTO로 반환
   */
  @Transactional(readOnly = true)
  public Page<ParticipantResult> getParticipants(Long challengeId, Pageable pageable) {
    return participationRepository
            .findActiveParticipantsByChallengeId(challengeId, pageable)
            .map(challengeMapper::toParticipantResult);
  }

  /**
   * 특정 멤버의 챌린지 참여 상태 조회
   */
  @Transactional(readOnly = true)
  public ChallengeParticipation getParticipation(Long challengeId, UUID userId) {
    return participationRepository.findByChallenge_IdAndUser_IdAndJoinOutIsNull(challengeId, userId)
            .orElseThrow(() -> new NotFoundException("해당 챌린지에 참여하지 않았습니다"));
  }

  /**
   * 특정 멤버의 모든 참여 현황 조회
   */
  @Transactional(readOnly = true)
  public Page<ParticipantResult> getUserParticipations(UUID userId, Pageable pageable) {
    Page<ChallengeParticipation> participations = participationRepository.findByUser_IdAndJoinOutIsNull(userId, pageable);
    return participations.map(challengeMapper::toParticipantResult);
  }


  /**
   * 참여 상태 시간 설정 (테스트 용도)
   */
  public void setJoinedAt(ChallengeParticipation participation, LocalDateTime joinedAt) {
    participation.setJoinedAt(joinedAt);
    participationRepository.save(participation);
  }
}