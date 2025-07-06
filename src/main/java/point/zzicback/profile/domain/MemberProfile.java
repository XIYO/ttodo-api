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
@Table(name = "member_profiles")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class MemberProfile {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private UUID memberId;
    
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Theme theme = Theme.LIGHT;
    
    @Lob
    private byte[] profileImage;
    
    @Column(length = 50)
    private String profileImageType;
    
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;
    
    public MemberProfile(UUID memberId) {
        this.memberId = memberId;
        this.theme = Theme.LIGHT;
    }
    
    public void updateTheme(Theme theme) {
        this.theme = theme;
    }
    
    public void updateProfileImage(byte[] profileImage, String imageType) {
        this.profileImage = profileImage;
        this.profileImageType = imageType;
    }
    
    public void removeProfileImage() {
        this.profileImage = null;
        this.profileImageType = null;
    }
}