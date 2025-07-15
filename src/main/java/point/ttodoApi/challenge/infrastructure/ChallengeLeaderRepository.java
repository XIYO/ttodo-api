package point.ttodoApi.challenge.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import point.ttodoApi.challenge.domain.Challenge;
import point.ttodoApi.challenge.domain.ChallengeLeader;
import point.ttodoApi.member.domain.Member;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * ChallengeLeader Repository
 */
@Repository
public interface ChallengeLeaderRepository extends JpaRepository<ChallengeLeader, Long> {
    
    /**
     * 특정 챌린지의 활성 리더 목록 조회
     */
    @Query("SELECT cl FROM ChallengeLeader cl " +
           "WHERE cl.challenge = :challenge AND cl.status = 'ACTIVE' " +
           "ORDER BY cl.appointedAt ASC")
    List<ChallengeLeader> findActiveLeadersByChallenge(@Param("challenge") Challenge challenge);
    
    /**
     * 특정 챌린지와 멤버로 리더 조회 (상태 무관)
     */
    @Query("SELECT cl FROM ChallengeLeader cl " +
           "WHERE cl.challenge = :challenge AND cl.member = :member")
    Optional<ChallengeLeader> findByChallengeAndMember(
        @Param("challenge") Challenge challenge, 
        @Param("member") Member member
    );
    
    /**
     * 특정 챌린지와 멤버로 활성 리더 조회
     */
    @Query("SELECT cl FROM ChallengeLeader cl " +
           "WHERE cl.challenge = :challenge AND cl.member = :member " +
           "AND cl.status = 'ACTIVE'")
    Optional<ChallengeLeader> findActiveLeaderByChallengeAndMember(
        @Param("challenge") Challenge challenge, 
        @Param("member") Member member
    );
    
    /**
     * 멤버가 리더인 모든 챌린지 조회
     */
    @Query("SELECT cl FROM ChallengeLeader cl " +
           "WHERE cl.member = :member AND cl.status = 'ACTIVE' " +
           "ORDER BY cl.appointedAt DESC")
    List<ChallengeLeader> findActiveChallengesByMember(@Param("member") Member member);
    
    /**
     * 멤버가 리더인 챌린지 수 조회
     */
    @Query("SELECT COUNT(cl) FROM ChallengeLeader cl " +
           "WHERE cl.member = :member AND cl.status = 'ACTIVE'")
    long countActiveChallengesByMember(@Param("member") Member member);
    
    /**
     * 챌린지의 활성 리더 수 조회
     */
    @Query("SELECT COUNT(cl) FROM ChallengeLeader cl " +
           "WHERE cl.challenge = :challenge AND cl.status = 'ACTIVE'")
    long countActiveLeadersByChallenge(@Param("challenge") Challenge challenge);
    
    /**
     * 특정 멤버가 특정 챌린지의 활성 리더인지 확인
     */
    @Query("SELECT COUNT(cl) > 0 FROM ChallengeLeader cl " +
           "WHERE cl.challenge = :challenge AND cl.member = :member " +
           "AND cl.status = 'ACTIVE'")
    boolean existsActiveLeaderByChallengeAndMember(
        @Param("challenge") Challenge challenge, 
        @Param("member") Member member
    );
    
    /**
     * 챌린지 ID로 활성 리더 목록 조회
     */
    @Query("SELECT cl FROM ChallengeLeader cl " +
           "WHERE cl.challenge.id = :challengeId AND cl.status = 'ACTIVE' " +
           "ORDER BY cl.appointedAt ASC")
    List<ChallengeLeader> findActiveLeadersByChallengeId(@Param("challengeId") Long challengeId);
    
    /**
     * 멤버 ID로 리더인 챌린지 목록 조회
     */
    @Query("SELECT cl.challenge FROM ChallengeLeader cl " +
           "WHERE cl.member.id = :memberId AND cl.status = 'ACTIVE' " +
           "ORDER BY cl.appointedAt DESC")
    List<Challenge> findActiveChallengesByMemberId(@Param("memberId") UUID memberId);
    
    /**
     * 특정 챌린지의 모든 리더 기록 조회 (제거된 리더 포함)
     */
    @Query("SELECT cl FROM ChallengeLeader cl " +
           "WHERE cl.challenge = :challenge " +
           "ORDER BY cl.appointedAt DESC")
    List<ChallengeLeader> findAllLeadersByChallengeOrderByAppointed(@Param("challenge") Challenge challenge);
    
    /**
     * 특정 기간 내에 임명된 리더 목록 조회
     */
    @Query("SELECT cl FROM ChallengeLeader cl " +
           "WHERE cl.challenge = :challenge AND cl.status = 'ACTIVE' " +
           "AND cl.appointedAt >= :fromDate " +
           "ORDER BY cl.appointedAt DESC")
    List<ChallengeLeader> findActiveLeadersAppointedAfter(
        @Param("challenge") Challenge challenge, 
        @Param("fromDate") java.time.LocalDateTime fromDate
    );
    
    /**
     * 특정 사용자가 임명한 리더 목록 조회
     */
    @Query("SELECT cl FROM ChallengeLeader cl " +
           "WHERE cl.challenge = :challenge AND cl.appointedBy = :appointedBy " +
           "ORDER BY cl.appointedAt DESC")
    List<ChallengeLeader> findLeadersAppointedBy(
        @Param("challenge") Challenge challenge, 
        @Param("appointedBy") UUID appointedBy
    );
    
    /**
     * 활성 리더가 있는 챌린지 ID 목록 조회
     */
    @Query("SELECT DISTINCT cl.challenge.id FROM ChallengeLeader cl " +
           "WHERE cl.status = 'ACTIVE'")
    List<Long> findChallengeIdsWithActiveLeaders();
    
    /**
     * 멤버가 활성 리더인 챌린지 ID 목록 조회
     */
    @Query("SELECT cl.challenge.id FROM ChallengeLeader cl " +
           "WHERE cl.member.id = :memberId AND cl.status = 'ACTIVE'")
    List<Long> findChallengeIdsByActiveLeaderMemberId(@Param("memberId") UUID memberId);
}