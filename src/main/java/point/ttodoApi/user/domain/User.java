package point.ttodoApi.user.domain;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;
import point.ttodoApi.shared.domain.BaseEntity;
import point.ttodoApi.user.domain.validation.ValidEmail;
import point.ttodoApi.user.domain.validation.ValidPassword;

import java.util.UUID;

/**
 * 사용자 인증 엔티티
 * <p>
 * 인증(Authentication) 관련 정보만 관리합니다.
 * 사용자 프로필 정보(닉네임, 타임존, 로케일 등)는
 * {@link point.ttodoApi.profile.domain.Profile}에서 관리합니다.
 * </p>
 *
 * @see point.ttodoApi.profile.domain.Profile
 */
@Entity
@Table(name = "\"user\"")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User extends BaseEntity {

  @Id
  private UUID id;

  @Setter
  @Column(unique = true, nullable = false)
  @ValidEmail
  private String email;

  @Setter
  @Column(nullable = false)
  @ValidPassword
  private String password;

  @PrePersist
  private void generateIdIfAbsent() {
    // Generate time-ordered UUID (v7-like) for new entities
    if (id == null) id = UuidCreator.getTimeOrdered();
  }
}
