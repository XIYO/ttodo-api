package point.zzicback.profile.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import point.zzicback.profile.domain.Profile;
import point.zzicback.profile.domain.Theme;
import point.zzicback.profile.infrastructure.persistence.ProfileRepository;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileService {
    
    private final ProfileRepository profileRepository;
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    
    @Transactional
    public Profile createProfile(UUID memberId) {
        if (profileRepository.existsByMemberId(memberId)) {
            throw new IllegalStateException("Profile already exists for member: " + memberId);
        }
        
        Profile profile = new Profile(memberId);
        return profileRepository.save(profile);
    }
    
    public Profile getProfile(UUID memberId) {
        return profileRepository.findByMemberId(memberId)
                .orElseGet(() -> createProfile(memberId));
    }
    
    @Transactional
    public Profile saveProfile(Profile profile) {
        return profileRepository.save(profile);
    }
    
    @Transactional
    public Profile updateTheme(UUID memberId, Theme theme) {
        Profile profile = getProfile(memberId);
        profile.updateTheme(theme);
        return profileRepository.save(profile);
    }
    
    @Transactional
    public Profile updateProfileImage(UUID memberId, MultipartFile file) throws IOException {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of 5MB");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }
        
        Profile profile = getProfile(memberId);
        profile.updateProfileImage(file.getBytes(), contentType);
        
        // 이미지 URL 설정
        String imageUrl = "/members/" + memberId + "/profile/image";
        profile.updateImageUrl(imageUrl);
        
        return profileRepository.save(profile);
    }
    
    @Transactional
    public Profile removeProfileImage(UUID memberId) {
        Profile profile = getProfile(memberId);
        profile.removeProfileImage();
        return profileRepository.save(profile);
    }
}