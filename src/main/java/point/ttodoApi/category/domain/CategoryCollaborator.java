package point.ttodoApi.category.domain;

import jakarta.persistence.*;
import lombok.*;
import point.ttodoApi.common.domain.BaseEntity;
import point.ttodoApi.member.domain.Member;

import java.time.LocalDateTime;

/**
 * 카테고리 협업자 엔티티
 * 카테고리의 owner가 다른 사용자를 협업자로 초대할 수 있으며,
 * 협업자는 owner와 동일한 권한으로 해당 카테고리의 Todo를 관리할 수 있습니다.
 */
@Entity
@Table(name = "category_collaborators",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = {"category_id", "member_id"})
    },
    indexes = {
        @Index(name = "idx_category_collaborator_member", columnList = "member_id"),
        @Index(name = "idx_category_collaborator_status", columnList = "status"),
        @Index(name = "idx_category_collaborator_member_status", columnList = "member_id, status")
    }
)
@Getter
@Setter
@NoArgsConstructor
public class CategoryCollaborator extends BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;
    
    @Column(name = "invited_at", nullable = false)
    private LocalDateTime invitedAt;
    
    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private CollaboratorStatus status = CollaboratorStatus.PENDING;
    
    @Column(name = "invitation_message", columnDefinition = "TEXT")
    private String invitationMessage;
    
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    // 생성자
    public CategoryCollaborator(Category category, Member member) {
        this.category = category;
        this.member = member;
        this.invitedAt = LocalDateTime.now();
        this.status = CollaboratorStatus.PENDING;
    }
    
    // 초대 메시지와 함께 생성
    public CategoryCollaborator(Category category, Member member, String invitationMessage) {
        this(category, member);
        this.invitationMessage = invitationMessage;
    }
    
    /**
     * 초대 수락
     */
    public void accept() {
        if (this.status != CollaboratorStatus.PENDING) {
            throw new IllegalStateException("Only pending invitations can be accepted");
        }
        this.status = CollaboratorStatus.ACCEPTED;
        this.acceptedAt = LocalDateTime.now();
    }
    
    /**
     * 초대 거절
     */
    public void reject() {
        if (this.status != CollaboratorStatus.PENDING) {
            throw new IllegalStateException("Only pending invitations can be rejected");
        }
        this.status = CollaboratorStatus.REJECTED;
    }
    
    /**
     * 소프트 삭제 (협업자 제거)
     */
    public void softDelete() {
        this.deletedAt = LocalDateTime.now();
    }
    
    /**
     * 삭제 여부 확인
     */
    public boolean isDeleted() {
        return this.deletedAt != null;
    }
    
    /**
     * 활성 협업자인지 확인 (수락됨 & 삭제되지 않음)
     */
    public boolean isActive() {
        return this.status == CollaboratorStatus.ACCEPTED && !isDeleted();
    }
}