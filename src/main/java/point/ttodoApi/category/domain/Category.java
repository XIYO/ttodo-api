package point.ttodoApi.category.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import point.ttodoApi.category.domain.validation.*;
import point.ttodoApi.user.domain.User;
import point.ttodoApi.shared.domain.BaseEntity;

import java.util.*;

import static point.ttodoApi.category.domain.CategoryConstants.*;
import java.util.stream.Collectors;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "categories",
        indexes = {
                @Index(name = "idx_category_user", columnList = "user_id")
        }
)
public class Category extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(nullable = false, length = NAME_MAX_LENGTH)
  @ValidCategoryName
  private String name;

  @Column(length = COLOR_LENGTH)
  @ValidColor
  private String color;

  @Column(length = DESCRIPTION_MAX_LENGTH)
  @ValidDescription
  private String description;

  @Column(name = "order_index")
  private Integer orderIndex = 0;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  @NotNull(message = OWNER_REQUIRED_MESSAGE)
  private User owner;

  @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
  private final Set<CategoryCollaborator> collaborators = new HashSet<>();

  @Builder
  public Category(String name, String color, String description, User owner, Integer orderIndex) {
    this.name = name;
    this.color = color;
    this.description = description;
    this.owner = owner;
    this.orderIndex = orderIndex != null ? orderIndex : 0;
  }

  public void update(String name, String color, String description, Integer orderIndex) {
    this.name = name;
    this.color = color;
    this.description = description;
    if (orderIndex != null) {
      this.orderIndex = orderIndex;
    }
  }

  /**
   * 협업자 확인 메서드
   * owner이거나 수락된 협업자인 경우 true 반환
   */
  public boolean isCollaborator(User user) {
    if (owner.equals(user)) return true;

    return collaborators.stream()
            .anyMatch(c -> c.getUser().equals(user)
                    && c.getStatus() == CollaboratorStatus.ACCEPTED
                    && !c.isDeleted());
  }

  /**
   * 관리 권한 확인 메서드
   * owner이거나 협업자인 경우 관리 권한 보유
   */
  public boolean canManage(User user) {
    return isCollaborator(user);
  }

  /**
   * 협업자 추가
   * owner는 협업자로 추가할 수 없음
   */
  public CategoryCollaborator addCollaborator(User user) {
    if (owner.equals(user)) throw new IllegalArgumentException("Owner cannot be a collaborator");

    // 이미 협업자인지 확인
    boolean alreadyExists = collaborators.stream()
            .anyMatch(c -> c.getUser().equals(user)
                    && !c.isDeleted()
                    && (c.getStatus() == CollaboratorStatus.PENDING
                    || c.getStatus() == CollaboratorStatus.ACCEPTED));

    if (alreadyExists) throw new IllegalArgumentException("User is already invited or is a collaborator");

    CategoryCollaborator collaborator = new CategoryCollaborator(this, user);
    collaborators.add(collaborator);

    return collaborator;
  }

  /**
   * 초대 메시지와 함께 협업자 추가
   */
  public CategoryCollaborator addCollaborator(User user, String invitationMessage) {
    if (owner.equals(user)) throw new IllegalArgumentException("Owner cannot be a collaborator");

    // 이미 협업자인지 확인
    boolean alreadyExists = collaborators.stream()
            .anyMatch(c -> c.getUser().equals(user)
                    && !c.isDeleted()
                    && (c.getStatus() == CollaboratorStatus.PENDING
                    || c.getStatus() == CollaboratorStatus.ACCEPTED));

    if (alreadyExists) throw new IllegalArgumentException("User is already invited or is a collaborator");

    CategoryCollaborator collaborator = new CategoryCollaborator(this, user, invitationMessage);
    collaborators.add(collaborator);

    return collaborator;
  }

  /**
   * 협업자 제거 (소프트 삭제)
   */
  public void removeCollaborator(User user) {
    collaborators.stream()
            .filter(c -> c.getUser().equals(user) && !c.isDeleted())
            .findFirst()
            .ifPresent(CategoryCollaborator::softDelete);
  }

  /**
   * 수락된 협업자 목록 조회
   */
  public Set<User> getAcceptedCollaborators() {
    return collaborators.stream()
            .filter(c -> c.getStatus() == CollaboratorStatus.ACCEPTED && !c.isDeleted())
            .map(CategoryCollaborator::getUser)
            .collect(Collectors.toSet());
  }

  /**
   * 활성 협업자 수 조회 (owner 제외)
   */
  private long getActiveCollaboratorCount() {
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
  public boolean isOwner(User user) {
    return user != null && owner.equals(user);
  }

  /**
   * 소유권 확인 메서드 (Spring Security @PreAuthorize용)
   *
   * @param userId 확인할 멤버 ID
   * @return 소유자인지 여부
   */
  private boolean isOwn(UUID userId) {
    if (userId == null || owner == null) return false;
    return owner.getId().equals(userId);
  }

  /**
   * 관리 권한 확인 메서드 (Spring Security @PreAuthorize용)
   * owner이거나 협업자인 경우 관리 권한 보유
   *
   * @param userId 확인할 멤버 ID
   * @return 관리 권한 보유 여부
   */
  public boolean canManage(UUID userId) {
    if (userId == null) return false;

    // owner인 경우
    if (isOwn(userId)) return true;

    // 협업자인 경우
    return collaborators.stream()
            .anyMatch(c -> c.getUser().getId().equals(userId)
                    && c.getStatus() == CollaboratorStatus.ACCEPTED
                    && !c.isDeleted());
  }
}
