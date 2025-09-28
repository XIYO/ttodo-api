package point.ttodoApi.profile.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import point.ttodoApi.category.infrastructure.persistence.CategoryRepository;
import point.ttodoApi.user.domain.User;
import point.ttodoApi.user.infrastructure.persistence.UserRepository;
import point.ttodoApi.profile.domain.*;
import point.ttodoApi.profile.infrastructure.persistence.*;
import point.ttodoApi.todo.infrastructure.persistence.TodoRepository;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProfileService {

  private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
  private final ProfileRepository profileRepository;
  private final StatisticsRepository statisticsRepository;
  private final TodoRepository todoRepository;
  private final CategoryRepository categoryRepository;
  private final UserRepository UserRepository;

  @Transactional
  public Profile createProfile(UUID userId, String nickname) {
    if (profileRepository.existsByOwnerId(userId)) {
      throw new IllegalStateException("Profile already exists for user: " + userId);
    }
    
    var user = UserRepository.findById(userId)
            .orElseThrow(() -> new IllegalStateException("User not found: " + userId));

    Profile profile = Profile.builder()
            .owner(user)
            .nickname(nickname)
            .build();
    return profileRepository.save(profile);
  }

  public Profile getProfile(UUID userId) {
    return profileRepository.findByOwnerId(userId)
            .orElseThrow(() -> new IllegalStateException("Profile not found for user: " + userId));
  }

  @Transactional
  public Profile saveProfile(Profile profile) {
    return profileRepository.save(profile);
  }

  @Transactional
  public Profile updateTheme(UUID userId, Theme theme) {
    Profile profile = getProfile(userId);
    profile.setTheme(theme);
    return profileRepository.save(profile);
  }

  @Transactional
  public Profile updateProfileImage(UUID userId, MultipartFile file) throws IOException {
    if (file.getSize() > MAX_FILE_SIZE) {
      throw new IllegalArgumentException("File size exceeds maximum allowed size of 5MB");
    }

    String contentType = file.getContentType();
    if (contentType == null || !contentType.startsWith("image/")) {
      throw new IllegalArgumentException("File must be an image");
    }

    Profile profile = getProfile(userId);
    profile.setProfileImage(file.getBytes(), contentType);

    // 이미지 URL 설정
    String imageUrl = "/user/" + userId + "/profile/image";
    profile.setImageUrl(imageUrl);

    return profileRepository.save(profile);
  }

  @Transactional
  public Profile removeProfileImage(UUID userId) {
    Profile profile = getProfile(userId);
    profile.clearProfileImage();
    return profileRepository.save(profile);
  }

  @Transactional
  public void deleteByOwner(UUID userId) {
    profileRepository.deleteByOwnerId(userId);
  }
}
