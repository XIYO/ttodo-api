package point.ttodoApi.challenge.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.*;
import java.util.*;
import java.util.stream.Collectors;

import point.ttodoApi.member.domain.Member;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Challenge {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;
    
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDate startDate;
    
    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PeriodType periodType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ChallengeVisibility visibility = ChallengeVisibility.PUBLIC;
    
    @Column(unique = true)
    private String inviteCode;
    
    private Integer maxParticipants;
    
    @Column(nullable = false)
    private UUID creatorId;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false) 
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChallengeParticipation> participations = new ArrayList<>();
    
    @OneToMany(mappedBy = "challenge", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChallengeLeader> leaders = new ArrayList<>();

    // 팩토리 메서드 - 공개 챌린지
    public static Challenge createPublicChallenge(String title, String description, 
                                                  PeriodType periodType, LocalDate startDate, 
                                                  LocalDate endDate, UUID creatorId, 
                                                  Integer maxParticipants) {
        Challenge challenge = new Challenge();
        challenge.title = title;
        challenge.description = description;
        challenge.periodType = periodType;
        challenge.startDate = startDate;
        challenge.endDate = endDate;
        challenge.creatorId = creatorId;
        challenge.maxParticipants = maxParticipants;
        challenge.visibility = ChallengeVisibility.PUBLIC;
        challenge.createdAt = LocalDateTime.now();
        challenge.updatedAt = LocalDateTime.now();
        return challenge;
    }
    
    // 팩토리 메서드 - 초대 전용 챌린지
    public static Challenge createInviteOnlyChallenge(String title, String description, 
                                                      PeriodType periodType, LocalDate startDate, 
                                                      LocalDate endDate, UUID creatorId,
                                                      Integer maxParticipants) {
        Challenge challenge = createPublicChallenge(title, description, periodType, 
                                                   startDate, endDate, creatorId, maxParticipants);
        challenge.visibility = ChallengeVisibility.INVITE_ONLY;
        challenge.inviteCode = generateInviteCode();
        return challenge;
    }
    
    private static String generateInviteCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    public void update(String title, String description, PeriodType periodType, Integer maxParticipants) {
        this.title = title;
        this.description = description;
        this.periodType = periodType;
        this.maxParticipants = maxParticipants;
        this.updatedAt = LocalDateTime.now();
    }
    
    // 비즈니스 메서드
    public boolean isJoinable() {
        if (maxParticipants != null) {
            long activeCount = participations.stream()
                .filter(p -> p.getJoinOut() == null)
                .count();
            return activeCount < maxParticipants;
        }
        return true;
    }
    
    public boolean isActive() {
        LocalDate now = LocalDate.now();
        return !now.isBefore(startDate) && !now.isAfter(endDate);
    }
    
    public long getActiveParticipantCount() {
        return participations.stream()
            .filter(p -> p.getJoinOut() == null)
            .count();
    }
    
    // 리더 관리 메서드들
    
    /**
     * 챌린지 생성자인지 확인
     */
    public boolean isOwner(Member member) {
        return member != null && this.creatorId.equals(member.getId());
    }
    
    /**
     * 챌린지 생성자인지 확인 (ID로)
     */
    public boolean isOwner(UUID memberId) {
        return memberId != null && this.creatorId.equals(memberId);
    }
    
    /**
     * 활성 리더인지 확인
     */
    public boolean isLeader(Member member) {
        if (member == null) return false;
        
        return leaders.stream()
            .anyMatch(leader -> leader.getMember().equals(member) && leader.isActive());
    }
    
    /**
     * 활성 리더인지 확인 (ID로)
     */
    public boolean isLeader(UUID memberId) {
        if (memberId == null) return false;
        
        return leaders.stream()
            .anyMatch(leader -> leader.getMember().getId().equals(memberId) && leader.isActive());
    }
    
    /**
     * 참여자 관리 권한 확인 (owner 또는 leader)
     */
    public boolean canManageParticipants(Member member) {
        return isOwner(member) || isLeader(member);
    }
    
    /**
     * 참여자 관리 권한 확인 (ID로)
     */
    public boolean canManageParticipants(UUID memberId) {
        return isOwner(memberId) || isLeader(memberId);
    }
    
    /**
     * 리더 추가
     */
    public ChallengeLeader addLeader(Member member, UUID appointedBy) {
        if (member == null) {
            throw new IllegalArgumentException("Member cannot be null");
        }
        
        // 이미 활성 리더인지 확인
        if (isLeader(member)) {
            throw new IllegalArgumentException("Member is already a leader");
        }
        
        // 챌린지에 참여하지 않은 멤버는 리더가 될 수 없음
        if (!isParticipant(member)) {
            throw new IllegalArgumentException("Member must be a participant to become a leader");
        }
        
        ChallengeLeader leader = new ChallengeLeader(this, member, appointedBy);
        this.leaders.add(leader);
        
        return leader;
    }
    
    /**
     * 리더 제거
     */
    public void removeLeader(Member member, UUID removedBy, String reason) {
        if (member == null) {
            throw new IllegalArgumentException("Member cannot be null");
        }
        
        ChallengeLeader leader = leaders.stream()
            .filter(l -> l.getMember().equals(member) && l.isActive())
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Member is not an active leader"));
        
        leader.removeLeader(removedBy, reason);
    }
    
    /**
     * 활성 리더 목록 조회
     */
    public List<ChallengeLeader> getActiveLeaders() {
        return leaders.stream()
            .filter(ChallengeLeader::isActive)
            .collect(Collectors.toList());
    }
    
    /**
     * 활성 리더 수 조회
     */
    public long getActiveLeaderCount() {
        return leaders.stream()
            .filter(ChallengeLeader::isActive)
            .count();
    }
    
    /**
     * 멤버가 챌린지 참여자인지 확인
     */
    public boolean isParticipant(Member member) {
        if (member == null) return false;
        
        return participations.stream()
            .anyMatch(p -> p.getMemberId().equals(member.getId()) && p.getJoinOut() == null);
    }
    
    /**
     * 멤버가 챌린지 참여자인지 확인 (ID로)
     */
    public boolean isParticipant(UUID memberId) {
        if (memberId == null) return false;
        
        return participations.stream()
            .anyMatch(p -> p.getMemberId().equals(memberId) && p.getJoinOut() == null);
    }
    
    /**
     * 챌린지에 대한 권한 확인 (owner, leader, participant 순서)
     */
    public ChallengeRole getMemberRole(Member member) {
        if (member == null) return ChallengeRole.NONE;
        
        if (isOwner(member)) return ChallengeRole.OWNER;
        if (isLeader(member)) return ChallengeRole.LEADER;
        if (isParticipant(member)) return ChallengeRole.PARTICIPANT;
        
        return ChallengeRole.NONE;
    }
    
    /**
     * 챌린지에 대한 권한 확인 (ID로)
     */
    public ChallengeRole getMemberRole(UUID memberId) {
        if (memberId == null) return ChallengeRole.NONE;
        
        if (isOwner(memberId)) return ChallengeRole.OWNER;
        if (isLeader(memberId)) return ChallengeRole.LEADER;
        if (isParticipant(memberId)) return ChallengeRole.PARTICIPANT;
        
        return ChallengeRole.NONE;
    }
    
    /**
     * 리더 최대 인원 제한 확인 (전체 참여자의 30% 또는 최소 1명)
     */
    public boolean canAddMoreLeaders() {
        long currentParticipants = getActiveParticipantCount();
        long currentLeaders = getActiveLeaderCount();
        
        // 참여자가 없으면 리더 추가 불가
        if (currentParticipants == 0) return false;
        
        // 최대 리더 수: 참여자의 30% (최소 1명, 최대 10명)
        long maxLeaders = Math.max(1, Math.min(10, currentParticipants * 30 / 100));
        
        return currentLeaders < maxLeaders;
    }
}
