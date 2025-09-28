package point.ttodoApi.challenge.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import point.ttodoApi.challenge.domain.*;
import point.ttodoApi.challenge.infrastructure.*;
import point.ttodoApi.user.domain.User;
import point.ttodoApi.user.infrastructure.persistence.UserRepository;

import java.util.*;

/**
 * 챌린지 리더 관리 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ChallengeLeaderService {

  private final ChallengeLeaderRepository leaderRepository;
  private final ChallengeRepository challengeRepository;
  private final UserRepository UserRepository;

  /**
   * 리더 임명
   */
  public ChallengeLeader appointLeader(Long challengeId, UUID userId, UUID appointedBy) {
    Challenge challenge = challengeRepository.findById(challengeId)
            .orElseThrow(() -> new IllegalArgumentException("Challenge not found: " + challengeId));

    User user = UserRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

    User appointer = UserRepository.findById(appointedBy)
            .orElseThrow(() -> new IllegalArgumentException("Appointer not found: " + appointedBy));

    // 임명 권한 확인 (챌린지 owner만 가능)
    if (!challenge.isOwner(appointer)) throw new IllegalArgumentException("Only challenge owner can appoint leaders");

    // 리더 추가 가능 여부 확인
    if (!challenge.canAddMoreLeaders())
      throw new IllegalArgumentException("Cannot add more leaders: maximum limit reached");

    ChallengeLeader leader = challenge.addLeader(user, appointedBy);
    ChallengeLeader savedLeader = leaderRepository.save(leader);

    log.info("User {} appointed as leader for challenge {} by {}",
            userId, challengeId, appointedBy);

    return savedLeader;
  }

  /**
   * 리더 해제
   */
  public void removeLeader(Long challengeId, UUID userId, UUID removedBy, String reason) {
    Challenge challenge = challengeRepository.findById(challengeId)
            .orElseThrow(() -> new IllegalArgumentException("Challenge not found: " + challengeId));

    User user = UserRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

    User remover = UserRepository.findById(removedBy)
            .orElseThrow(() -> new IllegalArgumentException("Remover not found: " + removedBy));

    // 해제 권한 확인 (챌린지 owner만 가능)
    if (!challenge.isOwner(remover)) throw new IllegalArgumentException("Only challenge owner can remove leaders");

    challenge.removeLeader(user, removedBy, reason);

    log.info("User {} removed from leader position for challenge {} by {} (reason: {})",
            userId, challengeId, removedBy, reason);
  }

  /**
   * 리더 자진 사퇴
   */
  public void resignLeader(Long challengeId, UUID userId, String reason) {
    Challenge challenge = challengeRepository.findById(challengeId)
            .orElseThrow(() -> new IllegalArgumentException("Challenge not found: " + challengeId));

    User user = UserRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

    // 본인이 리더인지 확인
    if (!challenge.isLeader(user)) throw new IllegalArgumentException("User is not a leader of this challenge");

    challenge.removeLeader(user, userId, reason != null ? reason : "자진 사퇴");

    log.info("User {} resigned from leader position for challenge {} (reason: {})",
            userId, challengeId, reason);
  }

  /**
   * 챌린지의 활성 리더 목록 조회
   */
  @Transactional(readOnly = true)
  public List<ChallengeLeader> getChallengeLeaders(Long challengeId, UUID requesterId) {
    Challenge challenge = challengeRepository.findById(challengeId)
            .orElseThrow(() -> new IllegalArgumentException("Challenge not found: " + challengeId));

    User requester = UserRepository.findById(requesterId)
            .orElseThrow(() -> new IllegalArgumentException("Requester not found: " + requesterId));

    // 접근 권한 확인 (챌린지 참여자만 가능)
    if (!challenge.isParticipant(requester))
      throw new IllegalArgumentException("Only challenge participants can view leaders");

    return leaderRepository.findActiveLeadersByChallenge(challenge);
  }

  /**
   * 멤버가 리더인 챌린지 목록 조회
   */
  @Transactional(readOnly = true)
  public List<Challenge> getUserLeaderChallenges(UUID userId) {
    User user = UserRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

    return leaderRepository.findActiveChallengesByuserId(userId);
  }

  /**
   * 멤버가 특정 챌린지의 리더인지 확인
   */
  @Transactional(readOnly = true)
  public boolean isLeader(Long challengeId, UUID userId) {
    Challenge challenge = challengeRepository.findById(challengeId)
            .orElseThrow(() -> new IllegalArgumentException("Challenge not found: " + challengeId));

    return challenge.isLeader(userId);
  }

  /**
   * 멤버의 챌린지 역할 조회
   */
  @Transactional(readOnly = true)
  public ChallengeRole getUserRole(Long challengeId, UUID userId) {
    Challenge challenge = challengeRepository.findById(challengeId)
            .orElseThrow(() -> new IllegalArgumentException("Challenge not found: " + challengeId));

    return challenge.getUserRole(userId);
  }

  /**
   * 챌린지의 리더 통계 조회
   */
  @Transactional(readOnly = true)
  public LeaderStatistics getLeaderStatistics(Long challengeId) {
    Challenge challenge = challengeRepository.findById(challengeId)
            .orElseThrow(() -> new IllegalArgumentException("Challenge not found: " + challengeId));

    long currentLeaders = challenge.getActiveLeaderCount();
    long maxLeaders = Math.max(1, Math.min(10, challenge.getActiveParticipantCount() * 30 / 100));
    boolean canAddMore = challenge.canAddMoreLeaders();

    return new LeaderStatistics(currentLeaders, maxLeaders, canAddMore);
  }

  /**
   * 리더 권한으로 참여자 관리 가능 여부 확인
   */
  @Transactional(readOnly = true)
  public boolean canManageParticipants(Long challengeId, UUID userId) {
    Challenge challenge = challengeRepository.findById(challengeId)
            .orElseThrow(() -> new IllegalArgumentException("Challenge not found: " + challengeId));

    return challenge.canManageParticipants(userId);
  }

  /**
   * 챌린지 리더 기록 조회 (제거된 리더 포함)
   */
  @Transactional(readOnly = true)
  public List<ChallengeLeader> getChallengeLeaderHistory(Long challengeId, UUID requesterId) {
    Challenge challenge = challengeRepository.findById(challengeId)
            .orElseThrow(() -> new IllegalArgumentException("Challenge not found: " + challengeId));

    User requester = UserRepository.findById(requesterId)
            .orElseThrow(() -> new IllegalArgumentException("Requester not found: " + requesterId));

    // 접근 권한 확인 (챌린지 owner만 가능)
    if (!challenge.isOwner(requester))
      throw new IllegalArgumentException("Only challenge owner can view leader history");

    return leaderRepository.findAllLeadersByChallengeOrderByAppointed(challenge);
  }

  /**
   * 리더 통계 DTO
   */
  public record LeaderStatistics(
          long currentLeaders,
          long maxLeaders,
          boolean canAddMore
  ) {
  }
}