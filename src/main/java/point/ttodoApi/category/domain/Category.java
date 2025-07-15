package point.ttodoApi.category.domain;

import jakarta.persistence.*;
import lombok.*;
import point.ttodoApi.member.domain.Member;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "categories",
    indexes = {
        @Index(name = "idx_category_owner", columnList = "owner_id")
    }
)
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private String name;

    @Column(length = 7)
    private String color;

    @Column(length = 255)
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private Member owner;
    
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CategoryCollaborator> collaborators = new HashSet<>();
    
    @Builder
    public Category(String name, String color, String description, Member owner) {
        this.name = name;
        this.color = color;
        this.description = description;
        this.owner = owner;
    }

    public void update(String name, String color, String description) {
        this.name = name;
        this.color = color;
        this.description = description;
    }
    
    /**
     * 협업자 확인 메서드
     * owner이거나 수락된 협업자인 경우 true 반환
     */
    public boolean isCollaborator(Member member) {
        if (member == null) return false;
        if (this.owner.equals(member)) return true;
        
        return collaborators.stream()
            .anyMatch(c -> c.getMember().equals(member) 
                && c.getStatus() == CollaboratorStatus.ACCEPTED
                && !c.isDeleted());
    }
    
    /**
     * 관리 권한 확인 메서드
     * owner이거나 협업자인 경우 관리 권한 보유
     */
    public boolean canManage(Member member) {
        return isCollaborator(member);
    }
    
    /**
     * 협업자 추가
     * owner는 협업자로 추가할 수 없음
     */
    public CategoryCollaborator addCollaborator(Member member) {
        if (this.owner.equals(member)) {
            throw new IllegalArgumentException("Owner cannot be a collaborator");
        }
        
        // 이미 협업자인지 확인
        boolean alreadyExists = collaborators.stream()
            .anyMatch(c -> c.getMember().equals(member) 
                && !c.isDeleted()
                && (c.getStatus() == CollaboratorStatus.PENDING 
                    || c.getStatus() == CollaboratorStatus.ACCEPTED));
        
        if (alreadyExists) {
            throw new IllegalArgumentException("Member is already invited or is a collaborator");
        }
        
        CategoryCollaborator collaborator = new CategoryCollaborator(this, member);
        this.collaborators.add(collaborator);
        
        return collaborator;
    }
    
    /**
     * 초대 메시지와 함께 협업자 추가
     */
    public CategoryCollaborator addCollaborator(Member member, String invitationMessage) {
        if (this.owner.equals(member)) {
            throw new IllegalArgumentException("Owner cannot be a collaborator");
        }
        
        // 이미 협업자인지 확인
        boolean alreadyExists = collaborators.stream()
            .anyMatch(c -> c.getMember().equals(member) 
                && !c.isDeleted()
                && (c.getStatus() == CollaboratorStatus.PENDING 
                    || c.getStatus() == CollaboratorStatus.ACCEPTED));
        
        if (alreadyExists) {
            throw new IllegalArgumentException("Member is already invited or is a collaborator");
        }
        
        CategoryCollaborator collaborator = new CategoryCollaborator(this, member, invitationMessage);
        this.collaborators.add(collaborator);
        
        return collaborator;
    }
    
    /**
     * 협업자 제거 (소프트 삭제)
     */
    public void removeCollaborator(Member member) {
        collaborators.stream()
            .filter(c -> c.getMember().equals(member) && !c.isDeleted())
            .findFirst()
            .ifPresent(CategoryCollaborator::softDelete);
    }
    
    /**
     * 수락된 협업자 목록 조회
     */
    public Set<Member> getAcceptedCollaborators() {
        return collaborators.stream()
            .filter(c -> c.getStatus() == CollaboratorStatus.ACCEPTED && !c.isDeleted())
            .map(CategoryCollaborator::getMember)
            .collect(Collectors.toSet());
    }
    
    /**
     * 활성 협업자 수 조회 (owner 제외)
     */
    public long getActiveCollaboratorCount() {
        return collaborators.stream()
            .filter(c -> c.getStatus() == CollaboratorStatus.ACCEPTED && !c.isDeleted())
            .count();
    }
    
    /**
     * 대기 중인 초대 수 조회
     */
    public long getPendingInvitationCount() {
        return collaborators.stream()
            .filter(c -> c.getStatus() == CollaboratorStatus.PENDING && !c.isDeleted())
            .count();
    }
    
    /**
     * 협업 가능한 카테고리인지 확인 (협업자가 있는지)
     */
    public boolean isCollaborative() {
        return getActiveCollaboratorCount() > 0;
    }
    
    /**
     * 특정 멤버가 이 카테고리의 owner인지 확인
     */
    public boolean isOwner(Member member) {
        return member != null && this.owner.equals(member);
    }
    
    /**
     * 소유권 확인 메서드 (Spring Security @PreAuthorize용)
     * @param memberId 확인할 멤버 ID
     * @return 소유자인지 여부
     */
    public boolean isOwn(UUID memberId) {
        if (memberId == null || this.owner == null) return false;
        return this.owner.getId().equals(memberId);
    }
    
    /**
     * 관리 권한 확인 메서드 (Spring Security @PreAuthorize용)
     * owner이거나 협업자인 경우 관리 권한 보유
     * @param memberId 확인할 멤버 ID
     * @return 관리 권한 보유 여부
     */
    public boolean canManage(UUID memberId) {
        if (memberId == null) return false;
        
        // owner인 경우
        if (isOwn(memberId)) return true;
        
        // 협업자인 경우
        return collaborators.stream()
            .anyMatch(c -> c.getMember().getId().equals(memberId)
                && c.getStatus() == CollaboratorStatus.ACCEPTED
                && !c.isDeleted());
    }
}
