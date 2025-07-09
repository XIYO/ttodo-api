package point.zzicback.profile.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Profile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private UUID memberId;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Theme theme = Theme.PINKY;
    
    @Column(length = 500)
    private String introduction;
    
    @Column(nullable = false, length = 50)
    private String timeZone = "Asia/Seoul";
    
    @Column(nullable = false, length = 10)
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
    
    public Profile(UUID memberId) {
        this.memberId = memberId;
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