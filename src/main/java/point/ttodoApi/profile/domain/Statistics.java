package point.ttodoApi.profile.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import point.ttodoApi.member.domain.Member;

import java.time.LocalDateTime;

@Entity
@Table(name = "statistics")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Statistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false, unique = true)
    private Member member;

    @Column
    private Integer succeededTodosCount;

    @Column
    private Integer succeededChallengesCount;

    @Column
    private Long totalFocusTime; // in seconds

    @Column
    private Integer currentStreakDays;

    @Column
    private Integer longestStreakDays;

    @Column
    private Integer categoryCount;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Builder
    public Statistics(Member member, Integer succeededTodosCount, Integer categoryCount) {
        this.member = member;
        this.succeededTodosCount = succeededTodosCount != null ? succeededTodosCount : 0;
        this.categoryCount = categoryCount != null ? categoryCount : 0;
        this.succeededChallengesCount = 0;
        this.totalFocusTime = 0L;
        this.currentStreakDays = 0;
        this.longestStreakDays = 0;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }
    
    /**
     * 통계 업데이트 (완료한 할일 수, 카테고리 수)
     */
    public void updateStatistics(Integer succeededTodosCount, Integer categoryCount) {
        this.succeededTodosCount = succeededTodosCount != null ? succeededTodosCount : 0;
        this.categoryCount = categoryCount != null ? categoryCount : 0;
        this.updatedAt = LocalDateTime.now();
    }
    
    @PrePersist
    private void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }
    
    @PreUpdate
    private void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
