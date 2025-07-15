package point.ttodoApi.member.domain;

import jakarta.persistence.*;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Member {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  @Setter(AccessLevel.NONE)
  private UUID id;
  @Column(unique = true, nullable = false)
  private String email;
  @Column(nullable = false)
  private String nickname;
  @Column(nullable = false)
  private String password;
  
  @CreatedDate
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;
  
  @LastModifiedDate
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;
  
  /**
   * 소유권 확인 메서드 (본인 확인)
   * @param memberId 확인할 멤버 ID
   * @return 본인인지 여부
   */
  public boolean isOwn(UUID memberId) {
    if (memberId == null) return false;
    return this.id != null && this.id.equals(memberId);
  }
}
