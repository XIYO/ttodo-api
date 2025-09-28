package point.ttodoApi.profile.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.*;
import org.springframework.lang.Nullable;
import point.ttodoApi.profile.domain.validation.*;
import point.ttodoApi.shared.domain.BaseEntity;
import point.ttodoApi.user.domain.User;

import java.util.UUID;

import static point.ttodoApi.profile.domain.ProfileConstants.*;

@Entity
@SoftDelete
@DynamicUpdate
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
@ToString(exclude = "profileImage")
public class Profile extends BaseEntity {

  @Id
  @UuidGenerator(style = UuidGenerator.Style.TIME)
  @EqualsAndHashCode.Include
  private UUID id;

  @OneToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "owner_id", nullable = false, unique = true)
  @NotNull(message = OWNER_ID_REQUIRED_MESSAGE)
  private User owner;

  @Column(nullable = false, length = NICKNAME_MAX_LENGTH)
  @ValidNickname
  @Setter
  private String nickname;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  @NotNull(message = THEME_REQUIRED_MESSAGE)
  @Builder.Default
  @Setter
  private Theme theme = DEFAULT_THEME;

  @Column(length = INTRODUCTION_MAX_LENGTH)
  @ValidIntroduction
  @Setter
  private String introduction;

  @Column(nullable = false, length = TIME_ZONE_MAX_LENGTH)
  @ValidTimeZone
  @Builder.Default
  @Setter
  private String timeZone = DEFAULT_TIME_ZONE;

  @Column(nullable = false, length = LOCALE_MAX_LENGTH)
  @ValidLocale
  @Builder.Default
  @Setter
  private String locale = DEFAULT_LOCALE;

  @Lob
  @Nullable
  private byte[] profileImage;

  @Column(length = IMAGE_TYPE_MAX_LENGTH)
  @ValidImageType
  @Nullable
  private String profileImageType;

  @Column(length = IMAGE_URL_MAX_LENGTH)
  @ValidImageUrl
  @Nullable
  @Setter
  private String imageUrl;


  /**
   * 프로필 이미지와 타입을 함께 설정
   *
   * @param profileImage 이미지 바이트 배열
   * @param imageType    이미지 타입
   */
  public void setProfileImage(byte[] profileImage, String imageType) {
    this.profileImage = profileImage;
    profileImageType = imageType;
  }

  /**
   * 프로필 이미지 관련 필드 모두 제거
   */
  public void clearProfileImage() {
    profileImage = null;
    profileImageType = null;
    imageUrl = null;
  }

}
