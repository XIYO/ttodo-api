package point.ttodoApi.challenge.infrastructure;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import point.ttodoApi.challenge.domain.*;
import point.ttodoApi.member.domain.Member;

import java.time.LocalDate;
import java.util.*;

public interface ChallengeTodoRepository extends JpaRepository<ChallengeTodo, Long> {

    Optional<ChallengeTodo> findByChallengeParticipation(ChallengeParticipation challengeParticipation);
    Optional<ChallengeTodo> findByChallengeParticipationAndTargetDate(ChallengeParticipation challengeParticipation, LocalDate targetDate);
    @Query("SELECT COUNT(DISTINCT ct.challengeParticipation.id) FROM ChallengeTodo ct WHERE ct.challengeParticipation.challenge.id = :challengeId AND ct.done = true")
    long countCompletedParticipantsByChallengeId(@Param("challengeId") Long challengeId);
    
    // 특정 날짜에 완료한 참여자 수
    @Query("SELECT COUNT(DISTINCT ct.challengeParticipation.id) FROM ChallengeTodo ct " +
           "WHERE ct.challengeParticipation.challenge.id = :challengeId " +
           "AND ct.targetDate = :date " +
           "AND ct.done = true")
    long countCompletedParticipantsForDate(
        @Param("challengeId") Long challengeId, 
        @Param("date") LocalDate date
    );
    
    // 참여자와 챌린지로 투두 수 조회
    long countByChallengeParticipation_Challenge_IdAndChallengeParticipation_Member_Id(Long challengeId, UUID memberId);
    
    // 사용자의 모든 챌린지 투두 조회
    @Query("SELECT ct FROM ChallengeTodo ct " +
           "JOIN FETCH ct.challengeParticipation cp " +
           "JOIN FETCH cp.challenge " +
           "WHERE cp.member = :member " +
           "AND cp.joinOut IS NULL")
    Page<ChallengeTodo> findAllByMember(@Param("member") Member member, Pageable pageable);
    
    // 특정 챌린지와 날짜로 투두 조회
    @Query("SELECT ct FROM ChallengeTodo ct " +
           "WHERE ct.challengeParticipation.challenge.id = :challengeId " +
           "AND ct.challengeParticipation.member = :member " +
           "AND ct.targetDate = :date")
    Optional<ChallengeTodo> findByChallengeMemberAndDate(
        @Param("challengeId") Long challengeId,
        @Param("member") Member member,
        @Param("date") LocalDate date
    );
}
