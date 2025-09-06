package point.ttodoApi.profile.domain;

import jakarta.persistence.*;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.annotation.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Profile {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true)
  private UUID ownerId;

  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  @Builder.Default
  private Theme theme = Theme.PINKY;

  @Column(length = 500)
  private String introduction;

  @Column(nullable = false, length = 50)
  @Builder.Default
  private String timeZone = "Asia/Seoul";

  @Column(nullable = false, length = 10)
  @Builder.Default
  private String locale = "ko-KR";

  @Lob
  private byte[] profileImage;

  @Column(length = 50)
  private String profileImageType;

  @Column(length = 500)
  private String imageUrl;

  @CreatedDate
  @Column(nullable = false, updatable = false)
  private LocalDateTime createdAt;

  @LastModifiedDate
  @Column(nullable = false)
  private LocalDateTime updatedAt;

  public Profile(UUID ownerId) {
    this.ownerId = ownerId;
    this.theme = Theme.PINKY;
    this.timeZone = "Asia/Seoul";
    this.locale = "ko-KR";
  }

  public void updateTheme(Theme theme) {
    this.theme = theme;
  }

  public void updateIntroduction(String introduction) {
    this.introduction = introduction;
  }

  public void updateTimeZone(String timeZone) {
    this.timeZone = timeZone;
  }

  public void updateLocale(String locale) {
    this.locale = locale;
  }

  public void updateProfileImage(byte[] profileImage, String imageType) {
    this.profileImage = profileImage;
    this.profileImageType = imageType;
  }

  public void updateImageUrl(String imageUrl) {
    this.imageUrl = imageUrl;
  }

  public void removeProfileImage() {
    this.profileImage = null;
    this.profileImageType = null;
    this.imageUrl = null;
  }
}