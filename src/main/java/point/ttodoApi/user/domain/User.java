package point.ttodoApi.user.domain;

import com.github.f4b6a3.uuid.UuidCreator;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;
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
@Setter // 일반 Setter 제공
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA 필수
@AllArgsConstructor(access = AccessLevel.PACKAGE) // MapStruct/테스트용
@Builder
@ToString(exclude = {"password"}) // 비밀번호는 toString에서 제외
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@FieldDefaults(level = AccessLevel.PRIVATE) // private 자동 적용
public class User extends BaseEntity {

  @EqualsAndHashCode.Include
  @Id
  @Builder.Default
  UUID id = UuidCreator.getTimeOrdered();

  @Column(unique = true, nullable = false)
  @ValidEmail
  String email;

  @Column(nullable = false)
  @ValidPassword
  String password;
  
  // === 도메인 메서드들 ===
  
  public boolean matchesPassword(String rawPassword) {
    // 실제로는 PasswordEncoder로 비교해야 함
    return this.password.equals(rawPassword);
  }
}
