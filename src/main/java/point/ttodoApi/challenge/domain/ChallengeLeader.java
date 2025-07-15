package point.ttodoApi.challenge.domain;

import jakarta.persistence.*;
import lombok.*;
import point.ttodoApi.member.domain.Member;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 챌린지 리더(그룹장) 엔티티
 */
@Entity
@Table(name = "challenge_leaders",
    indexes = {
        @Index(name = "idx_challenge_leader_challenge", columnList = "challenge_id"),
        @Index(name = "idx_challenge_leader_member", columnList = "member_id"),
        @Index(name = "idx_challenge_leader_status", columnList = "challenge_id, status")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_challenge_leader", columnNames = {"challenge_id", "member_id"})
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChallengeLeader {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "challenge_id", nullable = false)
    private Challenge challenge;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    
    @Column(name = "appointed_at", nullable = false)
    private LocalDateTime appointedAt;
    
    @Column(name = "appointed_by", nullable = false)
    private UUID appointedBy; // 리더를 지정한 사람 (일반적으로 챌린지 생성자)
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LeaderStatus status = LeaderStatus.ACTIVE;
    
    @Column(name = "removed_at")
    private LocalDateTime removedAt;
    
    @Column(name = "removed_by")
    private UUID removedBy;
    
    @Column(name = "removal_reason", length = 500)
    private String removalReason;
    
    /**
     * 새 리더 임명 생성자
     */
    public ChallengeLeader(Challenge challenge, Member member, UUID appointedBy) {
        this.challenge = challenge;
        this.member = member;
        this.appointedBy = appointedBy;
        this.appointedAt = LocalDateTime.now();
        this.status = LeaderStatus.ACTIVE;
    }
    
    /**
     * 리더 해제
     */
    public void removeLeader(UUID removedBy, String reason) {
        if (this.status == LeaderStatus.REMOVED) {
            throw new IllegalStateException("Leader is already removed");
        }
        
        this.status = LeaderStatus.REMOVED;
        this.removedAt = LocalDateTime.now();
        this.removedBy = removedBy;
        this.removalReason = reason;
    }
    
    /**
     * 활성 리더인지 확인
     */
    public boolean isActive() {
        return this.status == LeaderStatus.ACTIVE;
    }
    
    /**
     * 리더 복원 (제거된 리더를 다시 활성화)
     */
    public void restoreLeader(UUID restoredBy) {
        if (this.status != LeaderStatus.REMOVED) {
            throw new IllegalStateException("Only removed leader can be restored");
        }
        
        this.status = LeaderStatus.ACTIVE;
        this.removedAt = null;
        this.removedBy = null;
        this.removalReason = null;
        // appointedBy를 복원한 사람으로 업데이트
        this.appointedBy = restoredBy;
        this.appointedAt = LocalDateTime.now();
    }
}

/**
 * 리더 상태
 */
public enum LeaderStatus {
    /**
     * 활성 리더
     */
    ACTIVE,
    
    /**
     * 제거된 리더
     */
    REMOVED
}