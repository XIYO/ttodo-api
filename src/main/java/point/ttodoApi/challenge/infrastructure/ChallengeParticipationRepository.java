package point.ttodoApi.challenge.infrastructure;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import point.ttodoApi.challenge.domain.ChallengeParticipation;
import point.ttodoApi.member.domain.Member;

import java.util.*;

public interface ChallengeParticipationRepository extends JpaRepository<ChallengeParticipation, Long> {
    // 활성 참여자 조회 (joinOut이 null인 경우)
    List<ChallengeParticipation> findByMemberAndJoinOutIsNull(Member member);
    Optional<ChallengeParticipation> findByMemberAndChallenge_IdAndJoinOutIsNull(Member member, Long challengeId);
    boolean existsByMemberAndChallenge_IdAndJoinOutIsNull(Member member, Long challengeId);
    
    // 챌린지 ID와 멤버 ID로 참여 여부 확인
    boolean existsByChallenge_IdAndMember_IdAndJoinOutIsNull(Long challengeId, UUID memberId);
    
    // 챌린지 ID와 멤버 ID로 참여 정보 조회
    Optional<ChallengeParticipation> findByChallengeIdAndMemberId(Long challengeId, UUID memberId);
    
    // 챌린지의 참여자 목록 조회 (페이지네이션)
    @Query("SELECT cp FROM ChallengeParticipation cp " +
           "JOIN FETCH cp.member " +
           "WHERE cp.challenge.id = :challengeId " +
           "AND cp.joinOut IS NULL")
    Page<ChallengeParticipation> findActiveParticipantsByChallengeId(
        @Param("challengeId") Long challengeId, 
        Pageable pageable
    );
    
    // 특정 멤버의 챌린지 참여 상태 조회
    Optional<ChallengeParticipation> findByChallenge_IdAndMember_IdAndJoinOutIsNull(Long challengeId, UUID memberId);
    
    // 특정 멤버의 모든 참여 현황 조회
    Page<ChallengeParticipation> findByMember_IdAndJoinOutIsNull(UUID memberId, Pageable pageable);
}
