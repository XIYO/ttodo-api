package point.zzicback.profile.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import point.zzicback.profile.domain.MemberProfile;
import point.zzicback.profile.domain.Theme;
import point.zzicback.profile.infrastructure.persistence.MemberProfileRepository;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberProfileService {
    
    private final MemberProfileRepository memberProfileRepository;
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    
    @Transactional
    public MemberProfile createProfile(UUID memberId) {
        if (memberProfileRepository.existsByMemberId(memberId)) {
            throw new IllegalStateException("Profile already exists for member: " + memberId);
        }
        
        MemberProfile profile = new MemberProfile(memberId);
        return memberProfileRepository.save(profile);
    }
    
    public MemberProfile getProfile(UUID memberId) {
        return memberProfileRepository.findByMemberId(memberId)
                .orElseGet(() -> createProfile(memberId));
    }
    
    @Transactional
    public MemberProfile updateTheme(UUID memberId, Theme theme) {
        MemberProfile profile = getProfile(memberId);
        profile.updateTheme(theme);
        return memberProfileRepository.save(profile);
    }
    
    @Transactional
    public MemberProfile updateProfileImage(UUID memberId, MultipartFile file) throws IOException {
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum allowed size of 5MB");
        }
        
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }
        
        MemberProfile profile = getProfile(memberId);
        profile.updateProfileImage(file.getBytes(), contentType);
        return memberProfileRepository.save(profile);
    }
    
    @Transactional
    public MemberProfile removeProfileImage(UUID memberId) {
        MemberProfile profile = getProfile(memberId);
        profile.removeProfileImage();
        return memberProfileRepository.save(profile);
    }
}