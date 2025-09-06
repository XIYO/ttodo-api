package point.ttodoApi.profile.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import point.ttodoApi.category.infrastructure.persistence.CategoryRepository;
import point.ttodoApi.member.infrastructure.persistence.MemberRepository;
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
  private final MemberRepository memberRepository;

  @Transactional
  public Profile createProfile(UUID memberId) {
    if (profileRepository.existsByOwnerId(memberId)) {
      throw new IllegalStateException("Profile already exists for member: " + memberId);
    }

    Profile profile = new Profile(memberId);
    return profileRepository.save(profile);
  }

  public Profile getProfile(UUID memberId) {
    return profileRepository.findByOwnerId(memberId)
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